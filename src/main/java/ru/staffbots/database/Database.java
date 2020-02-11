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
import java.util.ArrayList;
import java.util.Collections;


public class Database {

    public static DBMS DBMSystem = DBMS.MySQL;

    public static String SERVER = "localhost";

    public static Integer PORT = 3306;

    public static String NAME = "staffbot";

    public static String USER = "root";

    public static String PASSWORD = "root";

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
            Journal.add(null);
            configs = new Configs();
            journal = new Journal();
            settings = new Settings();
            users = new Users();
            cleaner = new Cleaner();
            Journal.add("init_database", NAME);
        } catch (Exception exception) {
            connection = null;
            Database.exception = exception;
            Journal.add(NoteType.ERROR, "init_database", NAME, exception.getMessage());
        }
        return connected();
    }

    private static boolean dbExists() throws Exception{
        if(!connected())return false;
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

    public static ArrayList<String> getTableList(){
        ArrayList<String> result = new ArrayList(0);
        if(!connected()) return result;
        try {
            Statement statement = connection.createStatement();
            if(!statement.execute("SELECT table_name " +
                    "FROM information_schema.TABLES " +
                    "WHERE table_schema like '"+NAME+"'"))
                return  result;
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next())
                result.add(resultSet.getString(1));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        Collections.sort(result);
        return result;
    }


    public static String getTableNote(String tableName){
        if (journal.getTableName().equalsIgnoreCase(tableName)) return journal.getNote();
        if (configs.getTableName().equalsIgnoreCase(tableName)) return configs.getNote();
        if (settings.getTableName().equalsIgnoreCase(tableName)) return settings.getNote();
        if (users.getTableName().equalsIgnoreCase(tableName)) return users.getNote();
        for (Lever lever : Levers.list)
            if (lever.toValue().getTableName().equalsIgnoreCase(tableName))
                return lever.toValue().getNote();
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                if (value.getTableName().equalsIgnoreCase(tableName))
                    return value.getNote();
        return "";
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
