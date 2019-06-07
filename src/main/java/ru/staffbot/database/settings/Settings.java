package ru.staffbot.database.settings;

import ru.staffbot.database.DBTable;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.utils.Converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Settings extends DBTable {

    public static final String DB_TABLE_NAME = "sys_settings";
    public static final String DB_TABLE_FIELDS = "settingname VARCHAR(50), settingvalue VARCHAR(100)";

    private String name;

    public String getName(){
        return name;
    }


    public Settings(String name){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
        this.name = name;
    }

    public void save(String value){
        if(!Database.connected()) return;
        if (value == null) value = "";
        try {
            String currentValue = load();
            if (value.equals(currentValue)) return;
            PreparedStatement statement = getStatement(
                (currentValue == null) ?
                "INSERT INTO " + getTableName() + " (settingvalue, settingname) VALUES (?, ?)" :
                "UPDATE " + getTableName() + " SET settingvalue = ? WHERE LOWER(settingname) LIKE LOWER(?)");
            statement.setString(1, value);
            statement.setString(2, name);
            statement.executeUpdate();
            Journal.add("В таблице " + getTableName() + " сделана запись: " + name + " = " + value);
        } catch (Exception exception) {
            Journal.add("Ошибка записи в таблицу " + getTableName() + ": "+ exception.getMessage(), NoteType.ERROR);
        }
    }

    public String load(){
        if(!Database.connected()) return null;
        String settingValue = null;
        try {
            PreparedStatement statement = getStatement(
            "SELECT settingvalue FROM " + getTableName() +
            " WHERE  LOWER(settingname) LIKE LOWER(?)");
            statement.setString(1, name);
            statement.execute();
            if (statement.execute()) {
                ResultSet resultSet = statement.getResultSet();
                if (resultSet.next())
                    settingValue = resultSet.getString(1);
            }
        } catch (Exception exception) {
            Journal.add("Ошибка чтения таблицы " + getTableName() + ": "+ exception.getMessage(), NoteType.ERROR);
            return null;
        }
        return settingValue;
    }

    public boolean loadAsBollean(String trueValue, boolean defaultValue){
        String stringValue = load();
        if(stringValue == null) return defaultValue;
        return stringValue.equalsIgnoreCase(trueValue);
    }

    public long loadAsLong(long defaultValue){
        String stringValue = load();
        if(stringValue == null) return defaultValue;
        try {
            return Long.valueOf(stringValue);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    public int delete(){
        if(!Database.connected()) return 0 ;
        try {
            PreparedStatement statement = getStatement(
                    "DELETE FROM " + getTableName() + " WHERE  LOWER(settingname) LIKE LOWER(?)");
            statement.setString(1, name);
            int recordCount = statement.executeUpdate();
            if (recordCount > 0)
                Journal.add("Из таблицы " + getTableName() + " удалена запись: " + name);
            return recordCount;
        } catch (Exception exception) {
            Journal.add("Ошибка при удалении записи из таблицы " + getTableName() + ": "+ exception.getMessage(), NoteType.ERROR);
            return 0;
        }
    }

}
