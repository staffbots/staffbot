package ru.staffbots.database.configs;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
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
    private static final String staticTableFields =
        "configname VARCHAR(100), configvalue VARCHAR(500)";

    public Configs(){
        super(staticTableName, staticTableFields);
    }

    private static final String condition = " WHERE LOWER(configname) LIKE LOWER(?)";

    private boolean checkName(String name){
        if (name == null) return false;
        if (name.equals("")) return false;
        return true;
    }

    public void save(String name) {
        if (!checkName(name)) return;
        try {
            statement = getStatement( getValue(name) == null ?
                "INSERT INTO " + getTableName() + " (configvalue, configname) VALUES (?, ?)" :
                "UPDATE " + getTableName() + " SET configvalue = ?" + condition);
            statement.setString(1, Levers.getNameValues().toString());
            statement.setString(2, name);
            statement.executeUpdate();
            statement.close();
            Journal.add("save_config", name);
        } catch (Exception e) {
            Journal.add(NoteType.ERROR, "save_config", name, e.getMessage());
        }
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
        Journal.add(NoteType.WARNING, "load_config", name);
    }

    public void delete(String name) {
        if (!checkName(name)) return;
        try {
            statement = getStatement("DELETE FROM " + getTableName() + condition);
            statement.setString(1, name);
            if (deleteFromTable(statement) > 0)
                Journal.add(NoteType.WARNING, "delete_config", name);
        } catch (Exception e) {
            Journal.add(NoteType.ERROR, "delete_config", name, e.getMessage());
        }
    }

    public ArrayList<String> getList() {
        ArrayList<String> result = new ArrayList<>(0);
        try {
            statement = getStatement("SELECT configname FROM " + getTableName());
            if (statement.execute()) {
                ResultSet resultSet = statement.getResultSet();
                while (resultSet.next())
                    result.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            Journal.add(NoteType.ERROR, "select_config", e.getMessage());
        }
        return result;
    }

    private String getValue(String name){
        try {
            statement = getStatement("SELECT configvalue FROM " + getTableName() + condition);
            statement.setString(1, name);
            if (statement.execute())
                return statement.getResultSet().next() ?
                    statement.getResultSet().getString(1) : null;
        } catch (Exception e) {
            Journal.add(NoteType.ERROR, "value_config", name, e.getMessage());
        }
        return null;
    }

}
