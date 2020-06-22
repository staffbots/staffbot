package ru.staffbots.database.configs;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.Executor;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.levers.Levers;

import java.util.*;

/*
 * Конфигурации параметров (рычагов) управления,
 * предоставляет возможность сохранять в БД и загружать из БД
 * все параметры (рычаги) управления в виде именованных конфигураций.
 * Экземпляр описан как статическое поле в классе Database
 */
public class Configs extends DBTable {

    private Configs(){
        super("sys_configs", "configname VARCHAR(50), configvalue TEXT");
    }

    private static Configs instance = null;

    private static final String condition = " WHERE LOWER(configname) LIKE LOWER(?)";

    private static boolean checkName(String name){
        if (name == null) return false;
        if (name.equals("")) return false;
        return true;
    }

    private static String loadConfigValue(String configName){
        Executor<String> executor = new Executor();
        return executor.execQuery(
                "SELECT configvalue FROM " + instance.getTableName() + condition,
                (resultSet) -> {
                    return resultSet.next() ? resultSet.getString(1) : null;
                },
                configName);
    }

    private static boolean configExists(String configName){
        Executor<Boolean> executor = new Executor();
        return executor.execQuery(
                "SELECT configname FROM " + instance.getTableName() + condition,
                (resultSet) -> {
                    return resultSet.next();
                },
                configName);
    }

    public static Configs getInstance() {
        if (instance == null)
            if (Database.connected())
                synchronized (Configs.class) {
                    if (instance == null)
                        instance = new Configs();
                }
        return instance;
    }

    public static void save(String configName) {
        if (!checkName(configName)) return;
        Executor executor = new Executor("save_config", configName);
        String update = configExists(configName) ?
                    "UPDATE " + instance.getTableName() + " SET configvalue = ?" + condition :
                    "INSERT INTO " + instance.getTableName() + " (configvalue, configname) VALUES (?, ?)";
        executor.execUpdate(update, Levers.toConfigValue(), configName);
    }

    public static void load(String configName) {
        if (!checkName(configName)) return;
        if (Levers.fromConfigValue(loadConfigValue(configName)))
            Journal.add(NoteType.INFORMATION, "load_config", configName);
    }

    public static void delete(String name) {
        if (!checkName(name)) return;
        Executor executor = new Executor<>("delete_config", name);
        executor.execUpdate("DELETE FROM " + instance.getTableName() + condition, name);
    }

    public static ArrayList<String> getList() {
        Executor<ArrayList<String>> executor = new Executor();
        return executor.execQuery(
                "SELECT configname FROM " + instance.getTableName(),
                (resultSet) -> {
                    ArrayList<String> result = new ArrayList<>(0);
                    while (resultSet.next())
                        result.add(resultSet.getString(1));
                    return result;
                });
    }

}
