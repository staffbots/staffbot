package ru.staffbots.database.configs;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Executor;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;

import java.io.StringReader;
import java.sql.ResultSet;
import java.util.*;

/*
 * Конфигурации параметров (рычагов) управления,
 * предоставляет возможность сохранять в БД и загружать из БД
 * все параметры (рычаги) управления в виде именованных конфигураций.
 * Экземпляр описан как статическое поле в классе Database
 */
public class Configs extends DBTable {

    private static final String staticTableName = "sys_configs";
    private static final String staticTableFields = "configname VARCHAR(100), configvalue VARCHAR(500)";

    public Configs(){
        super(staticTableName, Translator.getValue("database", "configs_table"), staticTableFields);
    }

    private static final String condition = " WHERE LOWER(configname) LIKE LOWER(?)";

    private boolean checkName(String name){
        if (name == null) return false;
        if (name.equals("")) return false;
        return true;
    }

    public void save(String name) {
        if (!checkName(name)) return;
        Executor executor = new Executor("save_config", name);
        String update = getValue(name) == null ?
                    "INSERT INTO " + getTableName() + " (configvalue, configname) VALUES (?, ?)" :
                    "UPDATE " + getTableName() + " SET configvalue = ?" + condition;
        executor.execUpdate(update, Levers.getNameValues().toString(), name);
    }

    public void load(String name) {
        if (!checkName(name)) return;
        String value = getValue(name);
        if (value == null) return;
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(value.substring(1, value.length() - 1).replace(", ", "\n")));
        } catch (Exception e) {
            return;
        }
        for (Lever lever : Levers.list)
            if (!lever.isGroup())
                if (properties.containsKey(lever.getName()))
                    lever.set(
                        Long.parseLong(
                            properties.getProperty(
                                lever.getName())));
        Journal.add(NoteType.INFORMATION, "load_config", name);
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

    private String getValue(String name){
        Executor<String> executor = new Executor();
        return executor.execQuery(
                "SELECT configvalue FROM " + getTableName() + condition,
                (resultSet) -> {
                    return resultSet.next() ? resultSet.getString(1) : null;
                },
                name);
    }

}
