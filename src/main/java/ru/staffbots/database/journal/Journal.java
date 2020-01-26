package ru.staffbots.database.journal;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.values.DateValue;
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

    private static final String staticTableName = "sys_journal";
    private static final String staticTableFields =
            "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), " +
            "noteType INT DEFAULT 0, " +
            "noteName VARCHAR(50) CHARACTER SET utf8, " +
            "noteVariables VARCHAR(500) CHARACTER SET utf8";

    public Journal(){
        super(staticTableName, staticTableFields);
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
        System.out.println(DateValue.toString(noteDate, dateFormat) + "  |  " + note.toString());
        if (isStorable) insertNote(note);
    }

    private static boolean insertNote(Note note){
        if(Database.disconnected())return false;
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "INSERT INTO " + staticTableName + " (moment, noteType, noteName, noteVariables) VALUES (?, ?, ?, ?)" );
            statement.setTimestamp(1, new Timestamp(note.getDate().getTime()));
            statement.setInt(2, note.getType().getValue());
            statement.setString(3, note.getName());
            statement.setString(4, note.getVariables());

            statement.executeUpdate();
            statement.close();
            return true;
        } catch (Exception exception) {
            //Journal.add("В журнал не добавлена запись: " + note.getValue(), NoteType.ERROR, exception, false);
            return false;
        }
    }

    public static boolean erase(){
        return (new Journal()).eraseTable();
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

    public ArrayList<Note> getJournal(Date fromDate, Date toDate, Map<Integer, Boolean> noteTypes, String searchString){
        period.set(fromDate, toDate);
        return getJournal(noteTypes, searchString);
    }

    public ArrayList<Note> getJournal(String fromDate, String toDate, Map<Integer, Boolean> noteTypes, String searchString){
        period.set(fromDate, toDate);
        return getJournal(noteTypes, searchString);
    }

    public ArrayList<Note> getJournal(Map<Integer, Boolean> noteTypes, String searchString){
        ArrayList<Note> journal = new ArrayList();
        try {
            String condition = null;
            for (NoteType noteType : NoteType.values())
                if (noteTypes.containsKey(noteType.getValue()))
                    if (noteTypes.get(noteType.getValue()))
                    {
                        condition = (condition == null) ? " (" : condition + " OR ";
                            condition += "(";
                            condition += "noteType = ";
                            condition += noteType.getValue();
                            condition += ")";
                    }
            condition = (condition == null) ? "(noteType = -1)" : condition + ")";
            String fromCondition = (period.getFromDate() == null) ? "" : " AND (? <= moment)";
            String toCondition = (period.getToDate() == null) ? "" : " AND (moment <= ?)";
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "SELECT * FROM (SELECT moment, noteType, noteName, noteVariables FROM " + getTableName()
                            + " WHERE " + condition + fromCondition + toCondition
                            //+ " AND (LOWER(noteValue) LIKE '%" + searchString.toLowerCase()+ "%')"
                            + " ORDER BY moment DESC LIMIT "  + getCount()
                            + ") sub ORDER BY moment ASC");

            if (period.getFromDate() != null)
                statement.setTimestamp(1,
                    new Timestamp(period.getFromDate().getTime()));

            if (period.getToDate() != null)
                statement.setTimestamp((period.getFromDate() == null) ? 1 : 2,
                    new Timestamp(period.getToDate().getTime()));

            if(statement.execute()) {
                ResultSet resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    Date date = new Date(resultSet.getTimestamp(1).getTime());
                    Note note = new Note(
                            date,
                            NoteType.valueOf(resultSet.getInt(2)),
                            resultSet.getString(3),
                            resultSet.getString(4));
                    if ((searchString == null) || searchString.trim().isEmpty())
                        journal.add(note);
                    else
                        if (note.toString().toLowerCase().indexOf(searchString.trim().toLowerCase()) > 0)
                            journal.add(note);
                }
            }

        } catch (SQLException exception) {
            //Journal.add("Журнал не сформирован", NoteType.ERROR, exception);
        }

        return journal;
    }

}
