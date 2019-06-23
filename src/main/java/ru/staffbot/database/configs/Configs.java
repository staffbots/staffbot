package ru.staffbot.database.configs;

import ru.staffbot.database.DBTable;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.utils.levers.Lever;

import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class Configs extends DBTable {

    private static final String DB_TABLE_NAME = "sys_configs";
    private static final String DB_TABLE_FIELDS = "configname VARCHAR(100), configvalue VARCHAR(500)";

    private String name;

    public String getName(){
        return name;
    }

    public Configs(String name){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
        this.name = name;
    }


    public void save() throws Exception {
        Map<String, String> config = new HashMap<>();
        for (Lever lever : ru.staffbot.utils.levers.Levers.list)
            if (!(lever.toValue().getName().trim().equals("") || (lever.toValue().getName()==null)))
                config.put(lever.toValue().getName(), Long.toString(lever.toValue().get()));
        delete(); // Удяляем что бы избежать повторений
        statement = getStatement("INSERT INTO " + getTableName() + " (configname, configvalue) VALUES (?, ?)");
        statement.setString(1, name);
        statement.setString(2, config.toString());
        statement.executeUpdate();
        statement.close();
        Journal.add("Конфигурация параметров управления сохранена под именем: " + name);
    }

    public void load() throws Exception {
        String value = "";
        statement = getStatement("SELECT configvalue FROM " + getTableName() + " WHERE (configname = ?)");
        statement.setString(1, name);
        if (statement.execute()) {
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next())
                value = resultSet.getString(1);
        }
        Map<String, String> config = new HashMap<>();
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(value.substring(1, value.length() - 1).replace(", ", "\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            config.put((String)e.getKey(), (String) e.getValue());
        }
        for (Lever lever : ru.staffbot.utils.levers.Levers.list)
            if (config.containsKey(lever.toValue().getName()))
                lever.toValue().set(Long.parseLong(config.get(lever.toValue().getName())));
        Journal.add("Загружена конфигурация параметров управления с именем: " + name);
    }

    public void delete() throws Exception {
        statement = getStatement("DELETE FROM " + getTableName() + " WHERE (configname = ?)");
        statement.setString(1, name);
        statement.executeUpdate();
        statement.close();
        Journal.add("Удалена конфигурация параметров управления с именем: " + name);
    }

    public static ArrayList<String> getList() throws Exception {
        Configs config = new Configs("");
        config.statement = config.getStatement("SELECT configname FROM " + DB_TABLE_NAME);
        ArrayList<String> result = new ArrayList<>(0);
        if (config.statement.execute()) {
            ResultSet resultSet = config.statement.getResultSet();
            while (resultSet.next())
                result.add(resultSet.getString(1));
        }
        return result;
    }
}
