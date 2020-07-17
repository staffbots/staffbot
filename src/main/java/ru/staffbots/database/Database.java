package ru.staffbots.database;

import ru.staffbots.database.tables.DBTable;
import ru.staffbots.database.tables.LeversSets;
import ru.staffbots.database.tables.Variables;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.database.tables.users.Users;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.values.Value;

import java.sql.*;
import java.util.*;

public abstract class Database {

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
        server = value.trim();
    }

    ////////////////////////////////////////////////////////////////
    private static int port = 3306;

    public static int getPort() {
        return port;
    }

    public static void setPort(Integer value) {
        if (value == null) return;
        port = value;
    }

    ////////////////////////////////////////////////////////////////
    private static String name = "staffbot";

    public static String getName() {
        return name;
    }

    public static void setName(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        name = value.trim();
    }

    ////////////////////////////////////////////////////////////////
    private static String user = "pi";

    public static String getUser() {
        return user;
    }

    public static void setUser(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        user = value.trim();
    }

    ////////////////////////////////////////////////////////////////
    private static String password = "pi";

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String value) {
        if (value == null) return;
        password = value.trim();
    }

    ////////////////////////////////////////////////////////////////
    private static boolean drop = false;

    public static boolean isDrop() {
        return drop;
    }

    public static void setDrop(Boolean value) {
        if (value == null) return;
        drop = value;
    }

    ////////////////////////////////////////////////////////////////
    private static Map<String, DBTable> systemTableList = new HashMap(0);

    private static void addSystemTable(DBTable table) {
        systemTableList.put(table.getTableName(), table);
    }

    private static Connection connection = null;

    private static Exception exception = null;

    private static boolean databaseExists() throws Exception{
        if(disconnected())return false;
        boolean result = false;
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getCatalogs();
        while (resultSet.next())
            if (name.equals(resultSet.getString(1))) {
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
            executor.execUpdate("DROP DATABASE " + name);
            Journal.add(NoteType.WARNING, "drop_database", name);
            exists = false;
        }
        if(!exists) {
            executor.execUpdate("CREATE DATABASE " + name);
            Journal.add(NoteType.WARNING, "create_database", name);
        }
        return true;
    }

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
 
    public static boolean connect(){
        try {
            connection = DBMSystem.getConnection(server, port, user, password);
            createDatabase(drop);
            connection = DBMSystem.getConnection(server, port, name, user, password);
            addSystemTable(Journal.getInstance());
            Journal.add(null);
            Journal.add("init_database", name);
            addSystemTable(LeversSets.getInstance());
            addSystemTable(Variables.getInstance());
            addSystemTable(Users.getInstance());
            Languages.loadDefaultCode();
            Cleaner.restart();
        } catch (Exception e) {
            connection = null;
            exception = e;
            Journal.add(NoteType.ERROR, "init_database", name, exception.getMessage());
            exception.printStackTrace();
        }
        return connected();
    }

    public static ArrayList<String> findTables(String pattern){
        ArrayList<String> result = new ArrayList();
        if(disconnected())
            return result;
        try {
            DatabaseMetaData metaData = getConnection().getMetaData();
            ResultSet resultSet = metaData.getTables(name, null, pattern, null);
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
        for (Lever lever : Levers.getList())
            if (lever.isStorable())
                result.put(lever.getTableName(), lever.toValue());
        for (Device device : Devices.getList())
            for (Value value : device.getValues())
                if (value.isStorable())
                    result.put(value.toValue().getTableName(), value.toValue());
        if (withUnusingTables)
            for (String tableName : findTables(null)) // All tables list
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
