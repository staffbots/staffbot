package ru.staffbots.database.settings;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.Executor;
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
    private static final String staticTableFields = "settingname VARCHAR(50), settingvalue VARCHAR(100)";

    public Settings(){
        super(staticTableName, Translator.getValue("database", "settings_table"), staticTableFields);
    }

    public void save(String settingName, String settingValue){
        if (settingValue == null) settingValue = "";
        Executor executor = new Executor("save_settings", settingName, settingValue);
        String currentValue = load(settingName);
        if (settingValue.equals(currentValue)) return;
        String update = (currentValue == null) ?
                "INSERT INTO " + getTableName() + " (settingvalue, settingname) VALUES (?, ?)" :
                "UPDATE " + getTableName() + " SET settingvalue = ? WHERE LOWER(settingname) LIKE LOWER(?)";
        executor.execUpdate(update, settingValue, settingName);
    }

    public String load(String settingName){
        //  Executor<String> executor = new Executor("load_settings", settingName);
        Executor<String> executor = new Executor(null);
        return executor.execQuery(
                "SELECT settingvalue FROM " + getTableName() + " WHERE  LOWER(settingname) LIKE LOWER(?)",
                (resultSet) -> {
                    return (resultSet.next()) ? resultSet.getString(1) : null;
                }, settingName);
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

    public int delete(String settingName){
        Executor executor = new Executor("delete_settings", settingName);
        return executor.execUpdate(
                "DELETE FROM " + getTableName() + " WHERE  LOWER(settingname) LIKE LOWER(?)",
                settingName);
    }

}
