package ru.staffbots.database.settings;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.Executor;
import ru.staffbots.database.journal.Journal;

/*
 * Настройки,
 * предоставляет возможность сохранять и загружать в виде строки именованные настройки
 * Экземпляр описан как статическое поле в классе Database
 */
public class Settings extends DBTable {

    private Settings(){
        super("sys_settings", "settingname VARCHAR(50), settingvalue VARCHAR(100)");
    }

    private static Settings instance = null;

    public static Settings getInstance() {
        if (instance == null)
            if (Database.connected())
                synchronized (Settings.class) {
                    if (instance == null)
                        instance = new Settings();
                }
        return instance;
    }

    public static void save(String settingName, String settingValue){
        if (settingValue == null) settingValue = "";
        if (instance == null) return;
        Executor executor = new Executor("save_settings", settingName, settingValue);
        String currentValue = load(settingName);
        if (settingValue.equals(currentValue)) return;
        String update = (currentValue == null) ?
                "INSERT INTO " + instance.getTableName() + " (settingvalue, settingname) VALUES (?, ?)" :
                "UPDATE " + instance.getTableName() + " SET settingvalue = ? WHERE LOWER(settingname) LIKE LOWER(?)";
        executor.execUpdate(update, settingValue, settingName);
    }

    public static String load(String settingName){
        if (instance == null) return null;
        //  Executor<String> executor = new Executor("load_settings", settingName);
        Executor<String> executor = new Executor(null);
        return executor.execQuery(
                "SELECT settingvalue FROM " + instance.getTableName() + " WHERE  LOWER(settingname) LIKE LOWER(?)",
                (resultSet) -> {
                    return (resultSet.next()) ? resultSet.getString(1) : null;
                }, settingName);
    }

    public static String loadAsString(String name, String defaultValue){
        String stringValue = load(name);
        return (stringValue == null) ? defaultValue : stringValue;
    }

    public static boolean loadAsBollean(String name, String trueValue, boolean defaultValue){
        String stringValue = load(name);
        if(stringValue == null) return defaultValue;
        return stringValue.equalsIgnoreCase(trueValue);
    }

    public static long loadAsLong(String name, long defaultValue){
        String stringValue = load(name);
        if(stringValue == null) return defaultValue;
        try {
            return Long.valueOf(stringValue);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    public static int delete(String settingName){
        if (instance == null) return 0;
        Executor executor = new Executor("delete_settings", settingName);
        return executor.execUpdate(
                "DELETE FROM " + instance.getTableName() + " WHERE  LOWER(settingname) LIKE LOWER(?)",
                settingName);
    }

}
