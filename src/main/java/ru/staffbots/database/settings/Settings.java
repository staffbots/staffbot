package ru.staffbots.database.settings;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.Translator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/*
 * Настройки,
 * предоставляет возможность сохранять и загружать в виде строки именованные настройки
 * Экземпляр описан как статическое поле в классе Database
 */
public class Settings extends DBTable {

    private static final String staticTableName = "sys_settings";
    private static final String staticTableFields =
            "settingname VARCHAR(50), settingvalue VARCHAR(100)";

    public Settings(){
        super(staticTableName, Translator.getValue("database", "settings_table"), staticTableFields);
    }

    public void save(String name, String value){
        if(Database.disconnected()) return;
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
            Journal.add(NoteType.ERROR, "WriteTable", getTableName(), exception.getMessage());
        }
    }

    public String load(String name){
        if(Database.disconnected()) return null;
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
            Journal.add(NoteType.ERROR, "read_table", getTableName(), exception.getMessage());
            return null;
        }
        return settingValue;
    }

    public String loadAsString(String name, String defaultValue){
        String stringValue = load(name);
        return (stringValue == null) ? defaultValue : stringValue;
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
        if(Database.disconnected()) return 0 ;
        try {
            PreparedStatement statement = getStatement(
                    "DELETE FROM " + getTableName() + " WHERE  LOWER(settingname) LIKE LOWER(?)");
            statement.setString(1, name);
            int recordCount = statement.executeUpdate();
            if (recordCount > 0)
                Journal.add("Из таблицы " + getTableName() + " удалена запись: " + name);
            return recordCount;
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "delete_table", getTableName(), exception.getMessage());
            return 0;
        }
    }

}
