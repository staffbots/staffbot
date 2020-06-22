package ru.staffbots.database.journal;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.Executor;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.languages.Language;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.tools.values.LongValue;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * <b>Системный журнал</b><br>
 * Экземпляр описан как статическое поле в классе Database
 */
public class Journal extends DBTable {

    public static final String defaultNoteName = "any_message";

    private Journal(){
        super("sys_journal",
        "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), " +
             "noteType INT DEFAULT 0, " +
             "noteName VARCHAR(50) CHARACTER SET utf8, " +
             "noteVariables VARCHAR(500) CHARACTER SET utf8");
    }

    private static volatile Journal instance = null;

    public static Journal getInstance() {
        if (instance == null)
            if (Database.connected())
                synchronized (Journal.class) {
                    if (instance == null)
                        instance = new Journal();
                }
        return instance;
    }

    private static final long minNoteCount = 5;

    public static final long defaultNoteCount = 25;

    private static final long maxNoteCount = 99;

    public static final DateFormat dateFormat = DateFormat.DATETIME;

    public static void addAnyNote(String note){
        add(NoteType.INFORMATION, true, "any_message", note);
    }

    public static void addAnyNote(NoteType noteType, String note){
        add(noteType, true, "any_message", note);
    }

    public static void add(boolean isStorable, String noteName, String... noteVariables){
        add(NoteType.INFORMATION, isStorable, noteName, noteVariables);
    }

    public static void add(String noteName, String... noteVariables){
        add(NoteType.INFORMATION, true, noteName, noteVariables);
    }

    public static void add(NoteType noteType, String noteName, String... noteVariables){
        add(noteType, true, noteName, noteVariables);
    }

    public static void add(NoteType noteType, boolean isStorable, String noteName, String... noteVariables){
        Date noteDate = new Date();
        Note note = new Note(noteDate, noteType, noteName, noteVariables);
        System.out.println(note.getMessage(false, Languages.get()));
        if (isStorable) insertNote(note);
    }

    private static boolean insertNote(Note note){
        if (instance == null) return false;
        Executor executor = new Executor(null);
        return executor.execUpdate(
                "INSERT INTO " + instance.getTableName() + " (moment, noteType, noteName, noteVariables) VALUES (?, ?, ?, ?)",
                new Timestamp(note.getDate().getTime()).toString(),
                String.valueOf(note.getType().getValue()),
                note.getName(),
                note.getVariables()
                ) > 0;
    }

    public static ArrayList<Note> getJournal(Date fromDate, Date toDate, Map<Integer, Boolean> noteTypes, String searchStr, String countStr, Language language){
        return getJournal(new Period(dateFormat, fromDate, toDate), noteTypes, searchStr, countStr, language);
    }

    public static ArrayList<Note> getJournal(String fromDate, String toDate, Map<Integer, Boolean> noteTypes, String searchStr, String countStr, Language language){
        return getJournal(new Period(dateFormat, fromDate, toDate), noteTypes, searchStr, countStr, language);
    }

    public static ArrayList<Note> getJournal(Period period, Map<Integer, Boolean> noteTypes, String searchStr, String countStr, Language language){
        String condition = "((noteName IS NULL)";
        for (NoteType noteType : NoteType.values())
            if (noteTypes.containsKey(noteType.getValue()))
                if (noteTypes.get(noteType.getValue()))
                    condition += " OR (noteType = " + noteType.getValue() + ")";
        condition += ")";
        String fromCondition = (period.getFromDate() == null) ? "" : " AND (? <= moment)";
        String toCondition = (period.getToDate() == null) ? "" : " AND (moment <= ?)";
        String query ="SELECT moment, noteType, noteName, noteVariables FROM " + getInstance().getTableName()
                + " WHERE " + condition + fromCondition + toCondition
                + " ORDER BY moment DESC ";
        ArrayList<String> parameters = new ArrayList();
        if (period.getFromDate() != null)
            parameters.add(new Timestamp(period.getFromDate().getTime()).toString());
        if (period.getToDate() != null)
            parameters.add(new Timestamp(period.getToDate().getTime()).toString());

        long count = LongValue.fromString(countStr, defaultNoteCount);
        if (count > maxNoteCount) count = maxNoteCount;
        if (count < minNoteCount) count = minNoteCount;
        final long noteCount = count;

        Executor<ArrayList<Note>> executor = new Executor(null);
        return executor.execQuery(query,
                (resultSet) -> {
                    ArrayList<Note> result = new ArrayList();
                    while (resultSet.next() && (result.size() < noteCount)) {
                        Date date = new Date(resultSet.getTimestamp(1).getTime());
                        Note note = new Note(
                                date,
                                NoteType.valueOf(resultSet.getInt(2)),
                                resultSet.getString(3),
                                resultSet.getString(4));
                        if ((searchStr == null) || searchStr.trim().isEmpty())
                            result.add(note);
                        else if (note.toString(language).toLowerCase().indexOf(searchStr.trim().toLowerCase()) > -1)
                            result.add(note);
                    }
                    return result;
                },
                parameters.stream().toArray(String[]::new));
    }

}
