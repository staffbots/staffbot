package ru.staffbots.database.configs;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Executor;
import ru.staffbots.database.cleaner.Cleaner;
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

    private static final String staticTableName = "sys_configs";
    private static final String staticTableFields = "configname VARCHAR(50), configvalue TEXT";

    private Configs(){
        super(staticTableName, staticTableFields);
    }

    private static final Configs instance = new Configs();

    public static Configs getInstance() {
        return instance;
    }

    private static final String condition = " WHERE LOWER(configname) LIKE LOWER(?)";

    private boolean checkName(String name){
        if (name == null) return false;
        if (name.equals("")) return false;
        return true;
    }

    public void save(String configName) {
        if (!checkName(configName)) return;
        Executor executor = new Executor("save_config", configName);
        String update = getConfigValue(configName) == null ?
                    "INSERT INTO " + getTableName() + " (configvalue, configname) VALUES (?, ?)" :
                    "UPDATE " + getTableName() + " SET configvalue = ?" + condition;
        executor.execUpdate(update, Levers.toConfigValue(), configName);
    }

    public void load(String configName) {
        if (!checkName(configName)) return;
        if (Levers.fromConfigValue(getConfigValue(configName)))
            Journal.add(NoteType.INFORMATION, "load_config", configName);
    }

    public void delete(String name) {
        if (!checkName(name)) return;
        Executor executor = new Executor<>("delete_config", name);
        executor.execUpdate("DELETE FROM " + getTableName() + condition, name);
    }

    public ArrayList<String> getList() {
        Executor<ArrayList<String>> executor = new Executor();
        return executor.execQuery(
                "SELECT configname FROM " + getTableName(),
                (resultSet) -> {
                    ArrayList<String> result = new ArrayList<>(0);
                    while (resultSet.next())
                        result.add(resultSet.getString(1));
                    return result;
                });
    }

    private String getConfigValue(String configName){
        Executor<String> executor = new Executor();
        return executor.execQuery(
                "SELECT configvalue FROM " + getTableName() + condition,
                (resultSet) -> {
                    return resultSet.next() ? resultSet.getString(1) : null;
                },
                configName);
    }


}
