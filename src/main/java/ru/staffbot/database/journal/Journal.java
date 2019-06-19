package ru.staffbot.database.journal;

import ru.staffbot.database.DBTable;
import ru.staffbot.utils.Converter;
import ru.staffbot.database.Database;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.values.LongValue;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * <b>Системный журнал</b><br>
 *
 */
public class Journal extends DBTable {
    public static final long MAX_NOTE_COUNT = 99;
    public static final long DEFAULT_NOTE_COUNT = 20;
    public static final String DB_TABLE_NAME = "sys_journal";
    public static final String DB_TABLE_FIELDS = "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), note VARCHAR(255) CHARACTER SET utf8, noteType INT DEFAULT 0";
    public static final DateFormat DATE_FORMAT = DateFormat.DATETIME;

    public static void add(String note){
        add(note, NoteType.CONFIRM);
    }

    public static void add(String note, NoteType noteType){
        Date datetime = new Date();
        System.out.println(Converter.dateToString(datetime, DATE_FORMAT) + "  -  " + noteType + ":  " + note);
        insertNote(note, noteType);
    }

    public static boolean insertNote(String note, NoteType noteType){
        if(!Database.connected())return false;
        try {
            PreparedStatement ps = Database.getConnection().prepareStatement(
                    "INSERT INTO " + DB_TABLE_NAME + " (note, noteType) VALUES (?, ?)" );
            ps.setString(1, note);
            ps.setInt(2, noteType.getValue());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean erase(){
        return (new Journal()).eraseTable();
    }

    public Period period = new Period(DATE_FORMAT);

    private LongValue noteCount = new LongValue("","",false,
            1, DEFAULT_NOTE_COUNT, MAX_NOTE_COUNT);

    public void setCount(long newCount){
        noteCount.setValue(newCount);
    }

    public void setCount(String newCount){
        try {
            noteCount.setValue(Long.parseLong(newCount));
        } catch (NumberFormatException e) {
            noteCount.setDefaultValue();
        }
    }

    public long getCount(){
        return noteCount.getValue();
    }

    public Journal(){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
    }

    public Journal(String fromDate, String toDate){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
        period.set(fromDate, toDate);
    }

    public Journal(Date fromDate, Date toDate){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
        period.set(fromDate, toDate);
    }

    public ArrayList<Note> getJournal(Date fromDate, Date toDate, Map<Integer, Boolean> checkboxes, String searchString){
        period.set(fromDate, toDate);
        return getJournal(checkboxes, searchString);
    }

    public ArrayList<Note> getJournal(String fromDate, String toDate, Map<Integer, Boolean> checkboxes, String searchString){
        period.set(fromDate, toDate);
        return getJournal(checkboxes, searchString);
    }

    public ArrayList<Note> getJournal(Map<Integer, Boolean> typesForShow, String searchString){
        ArrayList<Note> journal = new ArrayList<>();
        try {
            String condition = null;
            for (NoteType noteType : NoteType.values())
                if (typesForShow.containsKey(noteType.getValue()))
                    if (typesForShow.get(noteType.getValue()))
                    {
                        condition = (condition == null) ? " (" : condition + " OR ";
                            condition += "(";
                            condition += "noteType = ";
                            condition += noteType.getValue();
                            condition += ")";
                    }
            condition = (condition == null) ? "(noteType = -1)" : condition + ")";
            String fromCondition = (period.fromDate == null) ? "" : " AND (? <= moment)";
            String toCondition = (period.toDate == null) ? "" : " AND (moment <= ?)";
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "SELECT * FROM (SELECT moment, note, noteType FROM " + getTableName()
                            + " WHERE " + condition + fromCondition + toCondition
                            + " AND (LOWER(note) LIKE '%" + searchString.toLowerCase()
                            + "%') ORDER BY moment DESC LIMIT "  + getCount()
                            + ") sub ORDER BY moment ASC");
            if (period.fromDate != null) {
                // Формат даты для журнала (DateFormat.TIMEDATE) не учитывает секунды,
                // которые прошли с начала минуты (для начальной даты):
                long time = period.fromDate.getTime() - period.fromDate.getTime() % DATE_FORMAT.accuracy.getMilliseconds();
                statement.setTimestamp(1, new Timestamp(time));
            }
            if (period.toDate != null) {
                // и которые остались до конца минуты (для конечной даты):
                long time = period.toDate.getTime() + (DATE_FORMAT.accuracy.getMilliseconds() - period.toDate.getTime() % DATE_FORMAT.accuracy.getMilliseconds());
                statement.setTimestamp((period.fromDate == null) ? 1 : 2, new Timestamp(time));
            }
            if(statement.execute()) {
                ResultSet resultSet = statement.getResultSet();
                while (resultSet.next()) {
                    Date date = new Date(resultSet.getTimestamp(1).getTime());
                    journal.add(new Note(date, resultSet.getString(2), NoteType.valueOf(resultSet.getInt(3))));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return journal;
    }

}
