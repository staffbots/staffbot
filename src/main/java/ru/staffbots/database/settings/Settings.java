package ru.staffbots.database.settings;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/*
 * Настройки,
 * предоставляет возможность сохранять и загружать в виде строки именованные настройки
 * Экземпляр описан как статическое поле в классе Database
 */
public class Settings extends DBTable {

    private static final String DB_TABLE_NAME = "sys_settings";
    private static final String DB_TABLE_FIELDS = "settingname VARCHAR(50), settingvalue VARCHAR(100)";

    public Settings(){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
    }

    public void save(String name, String value){
        if(!Database.connected()) return;
        if (value == null) value = "";
        try {
            String currentValue = load(name);
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

    public String load(String name){
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

    public boolean loadAsBollean(String name, String trueValue, boolean defaultValue){
        String stringValue = load(name);
        if(stringValue == null) return defaultValue;
        return stringValue.equalsIgnoreCase(trueValue);
    }

    public long loadAsLong(String name, long defaultValue){
        String stringValue = load(name);
        if(stringValue == null) return defaultValue;
        try {
            return Long.valueOf(stringValue);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    public int delete(String name){
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
