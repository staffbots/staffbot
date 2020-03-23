package ru.staffbots.database;

import ru.staffbots.database.cleaner.Cleaner;
import ru.staffbots.database.configs.Configs;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.database.settings.Settings;
import ru.staffbots.database.users.User;
import ru.staffbots.database.users.Users;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.values.Value;

import java.sql.*;
import java.util.*;

public class Database {

    public static DBMS DBMSystem = DBMS.MySQL;

    public static String SERVER = "localhost";

    public static Integer PORT = 3306;

    public static String NAME = "staffbot";

    public static String USER = "pi";

    public static String PASSWORD = "pi";

    public static Boolean DROP = false;

    public static Cleaner cleaner;

    public static Journal journal;

    public static Settings settings;

    public static Configs configs;

    public static Users users;

    private static Map<String, DBTable> systemTableList = new HashMap(0);

    private static Connection connection = null;

    private static Exception exception = null;

    public static Connection getConnection1 () {
        return connection;
    }

    public static Exception getException() {
        return exception;
    }

    public static boolean connected(){
        return (connection != null);
    }

    public static boolean disconnected(){
        return (connection == null);
    }
 
    public static boolean init(){
        try {
            connection = DBMSystem.getConnection(SERVER, PORT, new User(USER, PASSWORD));
            createDatabase(DROP);
            connection = DBMSystem.getConnection(SERVER, PORT, NAME, new User(USER, PASSWORD));
            journal = new Journal();
            Journal.add(null);
            Journal.add("init_database", NAME);
            configs = new Configs();
            settings = new Settings();
            users = new Users();
            systemTableList.put(journal.getTableName(), journal);
            systemTableList.put(configs.getTableName(), configs);
            systemTableList.put(settings.getTableName(), settings);
            systemTableList.put(users.getTableName(), users);
            cleaner = new Cleaner();
        } catch (Exception exception) {
            connection = null;
            Database.exception = exception;
            Journal.add(NoteType.ERROR, "init_database", NAME, exception.getMessage());
        }
        return connected();
    }

    private static boolean dbExists() throws Exception{
        if(disconnected())return false;
        boolean result = false;
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getCatalogs();
        while (resultSet.next())
            if (NAME.equals(resultSet.getString(1))) {
                result = true;
                break;
            }
        resultSet.close();
        return result;
    }

    private static boolean createDatabase(boolean drop) throws Exception{
        if(disconnected()) throw getException();
        boolean exists = dbExists();
        Executor executor = new Executor();
        if (exists && drop){
            executor.execUpdate("DROP DATABASE " + NAME);
            Journal.add(NoteType.WARNING, "drop_database", NAME);
            exists = false;
        } if(!exists) {
            executor.execUpdate("CREATE DATABASE " + NAME);
            Journal.add(NoteType.WARNING, "create_database", NAME);
        }
        return true;
    }

    public static Map<String, DBTable> getTableList(){
        return getTableList(true);
    }

    public static Map<String, DBTable> getTableList(boolean withSystemTables){
        return getTableList(true, withSystemTables);
    }

    private static Map<String, DBTable> getTableList(boolean withUnusingTables, boolean withSystemTables){
        Map<String, DBTable> result = new HashMap(0);
        if(disconnected()) return result;
        if (withSystemTables)
            result.putAll(systemTableList);
        for (Lever lever : Levers.list)
            if (lever.isStorable())
                result.put(lever.getTableName(), lever.toValue());
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                if (value.isStorable())
                    result.put(value.toValue().getTableName(), value.toValue());
        if (withUnusingTables) {
            ArrayList<String> tablesNames =
                    new Executor<ArrayList<String>>().execQuery(
                    "SELECT table_name " +
                            "FROM information_schema.TABLES " +
                            "WHERE table_schema like '"+NAME+"'",
                    (resultSet) -> {
                        ArrayList<String> handleResult = new ArrayList();
                        while (resultSet.next())
                            handleResult.add(resultSet.getString(1));
                        return handleResult;
                    }
            );
            for (String tableName : tablesNames)
                if (!systemTableList.containsKey(tableName))
                    if (!result.containsKey(tableName))
                        result.put(tableName, null);
        }
        return new TreeMap<String, DBTable>(result);
    }

    public static int dropUnusingTables(){
        int result = 0;
        Map<String, DBTable> tableList = getTableList(true, false);
        for (String tableName: tableList.keySet())
            if (tableList.get(tableName) == null)
                if (tableList.get(tableName).dropTable())
                    result++;
        return result;
    }

}
