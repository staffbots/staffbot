package ru.staffbots.database;

import ru.staffbots.database.cleaner.Cleaner;
import ru.staffbots.database.configs.Configs;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.database.settings.Settings;
import ru.staffbots.database.users.Users;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.values.Value;

import java.sql.*;
import java.util.*;

public class Database {

    ////////////////////////////////////////////////////////////////
    private static DBMS DBMSystem = DBMS.MySQL;

    public static DBMS getDBMSystem() {
        return DBMSystem;
    }

    ////////////////////////////////////////////////////////////////
    private static String server = "localhost";

    public static String getServer() {
        return server;
    }

    public static void setServer(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        server = value;
    }

    ////////////////////////////////////////////////////////////////
    public static Integer PORT = 3306;

    public static String NAME = "staffbot";

    public static String USER = "pi";

    public static String PASSWORD = "pi";

    public static Boolean DROP = false;

    public static Users users;

    private static Map<String, DBTable> systemTableList = new HashMap(0);

    private static Connection connection = null;

    private static Exception exception = null;

    public static Connection getConnection () {
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
            connection = DBMSystem.getConnection(server, PORT, USER, PASSWORD);
            createDatabase(DROP);
            connection = DBMSystem.getConnection(server, PORT, NAME, USER, PASSWORD);
            Journal.add(null);
            Journal.add("init_database", NAME);
            Languages.loadDefaultCode();
            users = new Users();
            systemTableList.put(Journal.getInstance().getTableName(), Journal.getInstance());
            systemTableList.put(Configs.getInstance().getTableName(), Configs.getInstance());
            systemTableList.put(Settings.getInstance().getTableName(), Settings.getInstance());
            systemTableList.put(users.getTableName(), users);
            Cleaner.getInstance().update();
        } catch (Exception e) {
            connection = null;
            exception = e;
            Journal.add(NoteType.ERROR, "init_database", NAME, exception.getMessage());
            exception.printStackTrace();
        }
        return connected();
    }

    private static boolean databaseExists() throws Exception{
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
        boolean exists = databaseExists();
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

    public static ArrayList<String> findTable(String pattern){
        ArrayList<String> result = new ArrayList();
        if(Database.disconnected())
            return result;
        try {
            DatabaseMetaData metaData = Database.getConnection().getMetaData();
            ResultSet resultSet = metaData.getTables(Database.NAME, null, pattern, null);
            while(resultSet.next())
                result.add(resultSet.getString("TABLE_NAME"));
            resultSet.close();
        } catch (SQLException exception) {
            Journal.add(NoteType.ERROR, "table_exists", pattern, exception.getMessage());
        }
        return result;
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
        if (withUnusingTables)
            for (String tableName : findTable(null)) // All tables list
                if (!systemTableList.containsKey(tableName))
                    if (!result.containsKey(tableName))
                        result.put(tableName, null);
        return new TreeMap<String, DBTable>(result);
    }

    public static int dropUnusingTables(){
        int result = 0;
        Map<String, DBTable> tableList = getTableList(true, false);
        for (String tableName: tableList.keySet())
            if (tableList.get(tableName) == null) {
                Executor executor = new Executor("drop_table", tableName);
                if (executor.execUpdate("DROP TABLE IF EXISTS " + tableName) > 0)
                    result++;
            }
        return result;
    }

}
