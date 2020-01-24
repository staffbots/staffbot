package ru.staffbots.database.configs;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;

import java.io.IOException;
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

    private static final String DB_TABLE_NAME = "sys_configs";
    private static final String DB_TABLE_FIELDS = "configname VARCHAR(100), configvalue VARCHAR(500)";

    public Configs(){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
    }

    private String Condition = " WHERE LOWER(configname) LIKE LOWER(?)";

    public void save(String name) throws Exception {
        Map<String, String> config = new HashMap<>();
        for (Lever lever : Levers.list)
            if (!lever.isGroup())
                config.put(lever.toValue().getName(), Long.toString(lever.toValue().get()));
        statement = getStatement( getValue(name) == null ?
            "INSERT INTO " + getTableName() + " (configvalue, configname) VALUES (?, ?)" :
            "UPDATE " + getTableName() + " SET configvalue = ?" + Condition );
        statement.setString(1, config.toString());
        statement.setString(2, name);
        statement.executeUpdate();
        statement.close();
        Journal.add("SaveConfig", name);
    }

    public void load(String name) throws Exception {
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
                if (properties.containsKey(lever.toValue().getName())) {
                    String val = lever.toString();
                    lever.toValue().set(
                        Long.parseLong(
                            properties.getProperty(
                                lever.toValue().getName())));
                    System.out.println( val + " => " +  lever.toString());
                }
        Journal.add(NoteType.WARNING, "LoadConfig", name);
    }

    public void delete(String name) throws Exception {
        statement = getStatement("DELETE FROM " + getTableName() + Condition);
        statement.setString(1, name);
        if (deleteFromTable(statement) > 0)
            Journal.add(NoteType.WARNING, "DeleteConfig", name);
    }

    public ArrayList<String> getList() throws Exception {
        statement = getStatement("SELECT configname FROM " + getTableName());
        ArrayList<String> result = new ArrayList<>(0);
        if (statement.execute()) {
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next())
                result.add(resultSet.getString(1));
        }
        return result;
    }

    private String getValue(String name) throws Exception {
        statement = getStatement("SELECT configvalue FROM " + getTableName() + Condition);
        statement.setString(1, name);
        if (statement.execute())
            return statement.getResultSet().next() ?
                statement.getResultSet().getString(1) : null;
        return null;
    }

}
