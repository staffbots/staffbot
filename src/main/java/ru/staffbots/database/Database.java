package ru.staffbots.database;

import ru.staffbots.database.cleaner.Cleaner;
import ru.staffbots.database.configs.Configs;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.database.settings.Settings;
import ru.staffbots.database.users.User;

import java.sql.*;

public class Database {

    public static final DBMS DBMSystem = DBMS.MySQL;

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
            settings = new Settings();
            cleaner = new Cleaner();
            configs = new Configs();
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

}
