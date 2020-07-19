package ru.staffbots.database.tables;

import ru.staffbots.database.Database;
import ru.staffbots.database.Executor;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.levers.Levers;

import java.util.ArrayList;

/**
 * Levers sets database table
 */
public class LeversSets extends DBTable {

    private LeversSets(){
        super("sys_leverssets", "setname VARCHAR(50) NOT NULL UNIQUE, setvalue TEXT");
    }

    private static LeversSets instance = null;

    private static final String condition = " WHERE LOWER(setname) LIKE LOWER(?)";

    private static boolean checkName(String name){
        if (name == null) return false;
        if (name.equals("")) return false;
        return true;
    }

    private static String loadConfigValue(String configName){
        Executor<String> executor = new Executor();
        return executor.execQuery(
                "SELECT setvalue FROM " + instance.getTableName() + condition,
                (resultSet) -> {
                    return resultSet.next() ? resultSet.getString(1) : null;
                },
                configName);
    }

    private static boolean configExists(String configName){
        Executor<Boolean> executor = new Executor();
        return executor.execQuery(
                "SELECT setname FROM " + instance.getTableName() + condition,
                (resultSet) -> {
                    return resultSet.next();
                },
                configName);
    }

    public static LeversSets getInstance() {
        if (instance == null)
            if (Database.connected())
                synchronized (LeversSets.class) {
                    if (instance == null)
                        instance = new LeversSets();
                }
        return instance;
    }

    public static void save(String setName) {
        if (!checkName(setName)) return;
        Executor executor = new Executor("save_leversset", setName);
        String update = configExists(setName) ?
                "UPDATE " + instance.getTableName() + " SET setvalue = ?" + condition :
                "INSERT INTO " + instance.getTableName() + " (setvalue, setname) VALUES (?, ?)";
        executor.execUpdate(update, Levers.toConfigValue(), setName);
    }

    public static void load(String setName) {
        if (!checkName(setName)) return;
        if (Levers.fromConfigValue(loadConfigValue(setName)))
            Journal.add(NoteType.INFORMATION, "load_leversset", setName);
    }

    public static void delete(String setName) {
        if (!checkName(setName)) return;
        Executor executor = new Executor<>("delete_leversset", setName);
        executor.execUpdate("DELETE FROM " + instance.getTableName() + condition, setName);
    }

    public static ArrayList<String> getList() {
        Executor<ArrayList<String>> executor = new Executor();
        return executor.execQuery(
                "SELECT setname FROM " + instance.getTableName(),
                (resultSet) -> {
                    ArrayList<String> result = new ArrayList<>(0);
                    while (resultSet.next())
                        result.add(resultSet.getString(1));
                    return result;
                });
    }

}
