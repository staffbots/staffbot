package ru.staffbot.database.journal;

import ru.staffbot.database.DBTable;
import ru.staffbot.utils.Converter;
import ru.staffbot.database.Database;
import ru.staffbot.utils.DateFormat;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * <b>Системный журнал</b><br>
 *
 */
public class Journal extends DBTable {

    public static final String DB_TABLE_NAME = "sys_jornal";
    public static final String DB_TABLE_FIELDS = "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), note VARCHAR(255) CHARACTER SET utf8, noteType INT DEFAULT 0";
    public static final DateFormat DATE_FORMAT = DateFormat.SHORTDATETIME;

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

    public Journal(){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
        period.set((String)null, (String)null);
    }

    public Journal(String fromDate, String toDate){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
        period.set(fromDate, toDate);
    }

    public Journal(Date fromDate, Date toDate){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
        period.set(fromDate, toDate);
    }

    public ArrayList<Note> getJournal(Date fromDate, Date toDate, Map<Integer, Boolean> checkboxes){
        period.set(fromDate, toDate);
        return getJournal(checkboxes);
    }

    public ArrayList<Note> getJournal(String fromDate, String toDate, Map<Integer, Boolean> checkboxes){
        period.set(fromDate, toDate);
        return getJournal(checkboxes);
    }

    public ArrayList<Note> getJournal(Map<Integer, Boolean> typesForShow){
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
            condition = (condition == null) ? "(noteType = -1) AND" : condition + ") AND ";
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "SELECT moment, note, noteType FROM " + getTableName() + " WHERE "
                            + condition
                            + "(? <= moment) AND (moment <= ?) ORDER BY moment");
            // Формат даты для журнала (DateFormat.TIMEDATE) не учитывает секунды,
            // которые прошли с начала минуты (для начальной даты):
            long time = period.fromDate.getValue().getTime() - period.fromDate.getValue().getTime() % DATE_FORMAT.accuracy.getValue();
            statement.setTimestamp(1, new Timestamp(time));
            // и которые остались до конца минуты (для конечной даты):
            time = period.toDate.getValue().getTime() + (DATE_FORMAT.accuracy.getValue() - period.toDate.getValue().getTime() % DATE_FORMAT.accuracy.getValue());
            statement.setTimestamp(2, new Timestamp(time));
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
