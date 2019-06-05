package ru.staffbot.database.configs;

import ru.staffbot.database.DBTable;
import ru.staffbot.database.Database;
import ru.staffbot.utils.levers.Lever;

import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class Configs extends DBTable {

    private static final String DB_TABLE_NAME = "sys_configs";
    private static final String DB_TABLE_FIELDS = "configname VARCHAR(50), configvalue TEXT";

    private PreparedStatement statement;

    private String name;

    public String getName(){
        return name;
    }

    public Configs(String name){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
        this.name = name;
    }

    private PreparedStatement getStatement(String query) throws Exception {
        if (!Database.connected())
            throw Database.getException();
        return Database.getConnection().prepareStatement(query);
    }

    public void save() throws Exception {
        Map<String, String> config = new HashMap<>();
        for (Lever lever : ru.staffbot.utils.levers.Levers.list)
            if (!(lever.getName().trim().equals("") || (lever.getName()==null)))
                config.put(lever.getName(), Long.toString(lever.get()));
        delete(); // Удяляем что бы избежать повторений
        statement = getStatement("INSERT INTO " + getTableName() + " (configname, configvalue) VALUES (?, ?)");
        statement.setString(1, name);
        statement.setString(2, config.toString());
        statement.executeUpdate();
        statement.close();
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
            if (config.containsKey(lever.getName()))
                lever.set(Long.parseLong(config.get(lever.getName())));
    }

    public void delete() throws Exception {
        statement = getStatement("DELETE FROM " + getTableName() + " WHERE (configname = ?)");
        statement.setString(1, name);
        statement.executeUpdate();
        statement.close();
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
