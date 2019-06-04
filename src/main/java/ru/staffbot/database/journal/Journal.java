package ru.staffbot.database.journal;

import ru.staffbot.database.journal.NoteType;
import ru.staffbot.utils.Converter;
import ru.staffbot.database.Database;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.values.DateValue;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * <b>Системный журнал</b><br>
 *
 */
public class Journal {

    public static final String DB_TABLE_NAME = "sys_jornal";

    public static DateFormat dateFormat = DateFormat.SHORTDATETIME;

    public Period period = new Period(dateFormat);

    public Journal(){
        period.set((String)null, (String)null);
    }

    public Journal(String fromDate, String toDate){
        period.set(fromDate, toDate);
    }

    public Journal(Date fromDate, Date toDate){
        period.set(fromDate, toDate);
    }

    public static void add(String note){
        add(note, NoteType.CONFIRM);
    }

    synchronized public static void add(String note, NoteType noteType){
        Date datetime = new Date();
        System.out.println(Converter.dateToString(datetime, dateFormat) + "  -  " + noteType + ":  " + note);
        insertNote(note, noteType);
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
                    "SELECT moment, note, noteType FROM " + DB_TABLE_NAME + " WHERE "
                            + condition
                            + "(? <= moment) AND (moment <= ?) ORDER BY moment");
            // Формат даты для журнала (DateFormat.TIMEDATE) не учитывает секунды,
            // которые прошли с начала минуты (для начальной даты):
            long time = period.fromDate.getValue().getTime() - period.fromDate.getValue().getTime() % dateFormat.accuracy.getValue();
            statement.setTimestamp(1, new Timestamp(time));
            // и которые остались до конца минуты (для конечной даты):
            time = period.toDate.getValue().getTime() + (dateFormat.accuracy.getValue() - period.toDate.getValue().getTime() % dateFormat.accuracy.getValue());
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


}
