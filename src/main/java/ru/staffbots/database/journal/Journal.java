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

    private static final String DB_TABLE_NAME = "sys_journal";
    private static final String DB_TABLE_FIELDS = "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), noteValue VARCHAR(255) CHARACTER SET utf8, noteType INT DEFAULT 0";

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

    private static final long MAX_NOTE_COUNT = 99;
    private static final long DEFAULT_NOTE_COUNT = 20;
    public static final DateFormat DATE_FORMAT = DateFormat.DATETIME;

    public static void add(String note, boolean isStorable){
        add(note, NoteType.CONFIRM, null, isStorable);
    }

    public static void add(String note){
        add(note, NoteType.CONFIRM);
    }

    public static void add(String note, NoteType noteType){
        add(note, NoteType.CONFIRM, null);
    }

    public static void add(String noteValue, NoteType noteType, Exception exception){
        add(noteValue, noteType, exception, true);
    }

    public static void add(String noteValue, NoteType noteType, Exception exception, boolean isStorable){
        Date noteDate = new Date();
        if (exception != null)
            if (NoteType.ERROR == noteType)
                noteValue += "<br>Сообщение: " + exception.getMessage()
                        + "<br>Стэк: "  + exception.getStackTrace();
        Note note = new Note(noteDate, noteValue, noteType);
        System.out.println(DateValue.toString(noteDate, DATE_FORMAT) + "  |  " + note);
        if (isStorable) insertNote(note);
    }

    private static boolean insertNote(Note note){
        if(!Database.connected())return false;
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "INSERT INTO " + DB_TABLE_NAME + " (moment, noteValue, noteType) VALUES (?, ?, ?)" );
            statement.setTimestamp(1, new Timestamp(note.getDate().getTime()));
            statement.setString(2, note.getValue());
            statement.setInt(3, note.getType().getValue());
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (Exception exception) {
            Journal.add("В журнал не добавлена запись: " + note.getValue(), NoteType.ERROR, exception, false);
            return false;
        }
    }

    public static boolean erase(){
        return (new Journal()).eraseTable();
    }

    public Period period = new Period(DATE_FORMAT);

    private LongValue noteCount = new LongValue("","", ValueMode.TEMPORARY,
            1, DEFAULT_NOTE_COUNT, MAX_NOTE_COUNT);

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
                    "SELECT * FROM (SELECT moment, noteValue, noteType FROM " + getTableName()
                            + " WHERE " + condition + fromCondition + toCondition
                            + " AND (LOWER(noteValue) LIKE '%" + searchString.toLowerCase()
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

        } catch (SQLException exception) {
            Journal.add("Журнал не сформирован", NoteType.ERROR, exception);
        }

        return journal;
    }

}
