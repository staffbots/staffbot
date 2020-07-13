package ru.staffbots.database.tables;

import ru.staffbots.database.Database;
import ru.staffbots.database.Executor;

/*
 * Variables,
 * предоставляет возможность сохранять и загружать в виде строки именованные настройки
 */
public class Variables extends DBTable {

    private Variables(){
        super("sys_variables", "variablename VARCHAR(50) NOT NULL UNIQUE, variablevalue VARCHAR(100)");
    }

    private static Variables instance = null;

    public static Variables getInstance() {
        if (instance == null)
            if (Database.connected())
                synchronized (Variables.class) {
                    if (instance == null)
                        instance = new Variables();
                }
        return instance;
    }

    public static void save(String name, String value){
        if (value == null) value = "";
        if (instance == null) return;
        Executor executor = new Executor("save_variables", name, value);
        String currentValue = load(name);
        if (value.equals(currentValue)) return;
        String update = (currentValue == null) ?
                "INSERT INTO " + instance.getTableName() + " (variablevalue, variablename) VALUES (?, ?)" :
                "UPDATE " + instance.getTableName() + " SET variablevalue = ? WHERE LOWER(variablename) LIKE LOWER(?)";
        executor.execUpdate(update, value, name);
    }

    public static String load(String name){
        if (instance == null) return null;
        //  Executor<String> executor = new Executor("load_variables", variableName);
        Executor<String> executor = new Executor(null);
        return executor.execQuery(
                "SELECT variablevalue FROM " + instance.getTableName() + " WHERE  LOWER(variablename) LIKE LOWER(?)",
                (resultSet) -> {
                    return (resultSet.next()) ? resultSet.getString(1) : null;
                }, name);
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

    public static int delete(String name){
        if (instance == null) return 0;
        Executor executor = new Executor("delete_variables", name);
        return executor.execUpdate(
                "DELETE FROM " + instance.getTableName() + " WHERE  LOWER(variablename) LIKE LOWER(?)",
                name);
    }

}
