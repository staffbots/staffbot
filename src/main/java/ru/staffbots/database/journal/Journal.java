package ru.staffbots.database.journal;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Executor;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.languages.Language;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.tools.values.LongValue;
import ru.staffbots.tools.values.ValueMode;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * <b>Системный журнал</b><br>
 * Экземпляр описан как статическое поле в классе Database
 */
public class Journal extends DBTable {

    public static final String defaultNoteName = "any_message";
    private static final String staticTableName = "sys_journal";
    private static final String staticTableFields =
            "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), " +
            "noteType INT DEFAULT 0, " +
            "noteName VARCHAR(50) CHARACTER SET utf8, " +
            "noteVariables VARCHAR(500) CHARACTER SET utf8";

    private Journal(){
        super(staticTableName, staticTableFields);
    }

    public static Journal getInstance() {
        return JournalHolder.HOLDER_INSTANCE;
    }

    private static class JournalHolder {
        private static final Journal HOLDER_INSTANCE = new Journal();
    }

    public Journal(String fromDate, String toDate){
        super(staticTableName, staticTableFields);
        period.set(fromDate, toDate);
    }

    public Journal(Date fromDate, Date toDate){
        super(staticTableName, staticTableFields);
        period.set(fromDate, toDate);
    }

    private static final long maxNoteCount = 99;

    private static final long defaultNoteCount = 20;

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
        Executor executor = new Executor(null);
        return executor.execUpdate(
                "INSERT INTO " + staticTableName + " (moment, noteType, noteName, noteVariables) VALUES (?, ?, ?, ?)",
                new Timestamp(note.getDate().getTime()).toString(),
                String.valueOf(note.getType().getValue()),
                note.getName(),
                note.getVariables()
                ) > 0;
    }

    public Period period = new Period(dateFormat);

    private LongValue noteCount = new LongValue("","", ValueMode.TEMPORARY, 1, defaultNoteCount, maxNoteCount);

    public void setCount(long newCount){
        noteCount.setValue(newCount);
    }

    public void setCount(String newCount){
        try {
            noteCount.setValue(Long.parseLong(newCount));
        } catch (NumberFormatException e) {
            noteCount.reset();
        }
    }

    public long getCount(){
        return noteCount.getValue();
    }

    public ArrayList<Note> getJournal(Date fromDate, Date toDate, Map<Integer, Boolean> noteTypes, String searchString, Language language){
        period.set(fromDate, toDate);
        return getJournal(noteTypes, searchString, language);
    }

    public ArrayList<Note> getJournal(String fromDate, String toDate, Map<Integer, Boolean> noteTypes, String searchString, Language language){
        period.set(fromDate, toDate);
        return getJournal(noteTypes, searchString, language);
    }

    public ArrayList<Note> getJournal(Map<Integer, Boolean> noteTypes, String searchString, Language language){
        String condition = "((noteName IS NULL)";
        for (NoteType noteType : NoteType.values())
            if (noteTypes.containsKey(noteType.getValue()))
                if (noteTypes.get(noteType.getValue()))
                    condition += " OR (noteType = " + noteType.getValue() + ")";
        condition += ")";
        String fromCondition = (period.getFromDate() == null) ? "" : " AND (? <= moment)";
        String toCondition = (period.getToDate() == null) ? "" : " AND (moment <= ?)";
        String query ="SELECT moment, noteType, noteName, noteVariables FROM " + getTableName()
                + " WHERE " + condition + fromCondition + toCondition
                + " ORDER BY moment DESC ";
        ArrayList<String> parameters = new ArrayList();
        if (period.getFromDate() != null)
            parameters.add(new Timestamp(period.getFromDate().getTime()).toString());
        if (period.getToDate() != null)
            parameters.add(new Timestamp(period.getToDate().getTime()).toString());
        Executor<ArrayList<Note>> executor = new Executor(null);
        return executor.execQuery(query,
                (resultSet) -> {
                    ArrayList<Note> result = new ArrayList();
                    while (resultSet.next() && (result.size() < getCount())) {
                        Date date = new Date(resultSet.getTimestamp(1).getTime());
                        Note note = new Note(
                                date,
                                NoteType.valueOf(resultSet.getInt(2)),
                                resultSet.getString(3),
                                resultSet.getString(4));
                        if ((searchString == null) || searchString.trim().isEmpty())
                            result.add(note);
                        else if (note.toString(language).toLowerCase().indexOf(searchString.trim().toLowerCase()) > -1)
                            result.add(note);
                    }
                    return result;
                },
                parameters.stream().toArray(String[]::new));
    }

}
