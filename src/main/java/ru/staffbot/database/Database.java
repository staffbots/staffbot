package ru.staffbot.database;

import ru.staffbot.database.cleaner.Cleaner;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.users.User;

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

    private static Connection connection;

    private static Exception exception = null;

    public static Connection getConnection () {
        return connection;
    }

    public static Exception getException() {
        return exception;
    }

    public static boolean connected(){
        return (exception != null);
    }

    public static boolean init(){
        try {
            connection = DBMSystem.getConnection(SERVER, PORT, new User(USER, PASSWORD));
            createDatabase(DROP);
            connection = DBMSystem.getConnection(SERVER, PORT, new User(USER, PASSWORD), NAME);
            journal = new Journal();
            cleaner = new Cleaner();
            Journal.add("База данных " + NAME + " готова к использованию");
        } catch (Exception exception) {
            Database.exception = exception;
            Journal.add("Ошибка инициализации базы данных " + NAME + " " + exception.getMessage());
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
            Journal.add("Удалена БД " + NAME, NoteType.WRINING);
            exists = false;
        } if(!exists) {
            Statement statement = connection.createStatement();
            statement.execute("CREATE DATABASE " + NAME);
            statement.close();
            Journal.add("Создана БД " + NAME, NoteType.WRINING);
        }
        return true;
    }

}
