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
import java.util.stream.Collectors;


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

    private static Connection connection = null;

    private static Exception exception;

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
            connection = DBMSystem.getConnection(SERVER, PORT, new User(USER, PASSWORD));
            createDatabase(DROP);
            connection = DBMSystem.getConnection(SERVER, PORT, new User(USER, PASSWORD), NAME);
            journal = new Journal();
            Journal.add(null);
            Journal.add("init_database", NAME);
        } catch (Exception exception) {
            connection = null;
            Database.exception = exception;
            Journal.add(NoteType.ERROR, "init_database", NAME, exception.getMessage());
        }
        if (connected()) {
            configs = new Configs();
            settings = new Settings();
            users = new Users();
            cleaner = new Cleaner();
        }
        return connected();
    }

    private static boolean dbExists() throws Exception{
        if(disconnected())return false;
        boolean result = false;
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getCatalogs();
        while (rs.next())
            if (NAME.equals(rs.getString(1))) {
                result = true;
                break;
            }
        rs.close();
        return result;
    }

    private static boolean createDatabase(boolean drop) throws Exception{
        if(!connected())return false;
        boolean exists = dbExists();
        if (exists && drop){
            Statement statement = connection.createStatement();
            statement.execute("DROP DATABASE " + NAME);
            statement.close();
            Journal.add(NoteType.WARNING, "drop_database", NAME);
            exists = false;
        } if(!exists) {
            Statement statement = connection.createStatement();
            statement.execute("CREATE DATABASE " + NAME);
            statement.close();
            Journal.add(NoteType.WARNING, "create_database", NAME);
        }
        return true;
    }

    public static Map<String, DBTable> getTableList(){
        return getTableList(true);
    }

    public static Map<String, DBTable> getTableList(boolean useOnly){
        Map<String, DBTable> result = new HashMap(0);
        if(!connected()) return result;
        result.put(journal.getTableName(), journal);
        result.put(configs.getTableName(), configs);
        result.put(settings.getTableName(), settings);
        result.put(users.getTableName(), users);
        for (Lever lever : Levers.list)
            if (lever.isStorable())
                result.put(lever.getTableName(), lever.toValue());
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                if (value.isStorable())
                    result.put(value.toValue().getTableName(), value.toValue());
        if (!useOnly)
        try {
            Statement statement = connection.createStatement();
            if(!statement.execute("SELECT table_name " +
                    "FROM information_schema.TABLES " +
                    "WHERE table_schema like '"+NAME+"'"))
                return  result;
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                if (!result.containsKey(tableName))
                    result.put(tableName, null);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return new TreeMap<String, DBTable>(result);
    }

    public static int dropUnuseTable(){
        int result = 0;
        Map<String, DBTable>tableList = getTableList(false);
        for (String tableName: tableList.keySet())
            if (tableList.get(tableName)== null)
                if (dropTable(tableName))
                    result++;
        return result;
    }

    public static boolean dropTable(String tableName){
        if(Database.disconnected())return false;
        try {
            if (tableExists(tableName)){
                getStatement("DROP TABLE " + tableName).execute();
                Journal.add(NoteType.WARNING, "drop_table", tableName);
            }
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "drop_table", tableName, exception.getMessage());
            return false;
        }
        return true;
    }

    public static PreparedStatement getStatement(String query) throws Exception {
        if (disconnected())
            throw getException();
        return getConnection().prepareStatement(query);
    }

    public static boolean tableExists(String tableName){
        if(disconnected())return false;
        try {
            DatabaseMetaData metaData = getConnection().getMetaData();
            ResultSet tables = metaData.getTables(Database.NAME, null, tableName, null);
            return (tables.next());
        } catch (SQLException exception) {
            Journal.add(NoteType.ERROR, "table_exists", tableName, exception.getMessage());
            return false;
        }
    }

    public static long getTableRows(String tableName){
        long result = 0;
        if(!connected()) return result;
        try {

            Statement statement = connection.createStatement();
            if(!statement.execute("SELECT COUNT(*) FROM " + tableName))
                return  result;
            ResultSet resultSet = statement.getResultSet();
            if(resultSet.next())
                return resultSet.getLong(1);

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return result;
    }

}
