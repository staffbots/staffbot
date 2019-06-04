package ru.staffbot.database;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.configs.Configs;
import ru.staffbot.database.settings.Settings;
import ru.staffbot.database.users.User;
import ru.staffbot.database.users.Users;

import java.sql.*;
import java.util.Date;


public class Database {

    public static DBMS DBMSystem = DBMS.MySQL;

    public static String SERVER = "localhost";

    public static Integer PORT = 3306;

    public static String NAME = "staffbot";

    public static String USER = "root";

    public static String PASSWORD = "root";

    public static Boolean DROP = false;

    private static Connection connection;

    public static Connection getConnection () {
        return connection;
    }

    private static Exception exception = new Exception("Попытки подключения не было");;

    public static Exception getException() {
        return exception;
    }

    public static boolean connected(){
        return (exception.getMessage().equals(""));
    }

    public static boolean connect(){
        exception = new Exception("");
        try {
            connection = DBMSystem.getConnection(SERVER, PORT, new User(USER, PASSWORD));
            createDatabase(DROP);
            connection = DBMSystem.getConnection(SERVER, PORT, new User(USER, PASSWORD), NAME);
            createTable(Journal.DB_TABLE_NAME, "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), note VARCHAR(255) CHARACTER SET utf8, noteType INT DEFAULT 0", DROP);
            createTable(Configs.DB_TABLE_NAME, " configname VARCHAR(50), configvalue TEXT", Database.DROP);
            createTable(Settings.DB_TABLE_NAME, " setting VARCHAR(50), value VARCHAR(50)", DROP);
            createTable(Users.DB_TABLE_NAME, " login VARCHAR(16), password VARCHAR(16), role INT", DROP);
            Database.eraseTable(Journal.DB_TABLE_NAME);
            Journal.add("База данных " + NAME + " готова к использованию");
        } catch (Exception exception) {
            Database.exception = exception;
        }
        return connected();
    }

    public static boolean init(){
        return connect();
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

    public static boolean tableExists(String tableName) throws Exception{
        if(!connected())return false;
        boolean result = false;
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(NAME,"","%",null);
        while (rs.next()) {
            if (tableName.toLowerCase().equals(rs.getString(3))) {
                result = true;
                break;
            }
        }
        rs.close();
        return result;
    }

    public static boolean createTable(String tableName, String sql, boolean drop){
        if(!connected())return false;
        try {
            boolean exists = tableExists(tableName);
            if (exists && drop){
                Statement statement = connection.createStatement();
                statement.execute("DROP TABLE " + tableName);
                Journal.add("Удалена таблица " + tableName, NoteType.WRINING);
                exists = false;
            }

            if(!exists) {
                Statement statement = connection.createStatement();
                statement.execute("CREATE TABLE " + tableName + " (" + sql + ")");
                Journal.add("Создана таблица " + tableName + " (" + sql + ")", NoteType.WRINING);
            }
        } catch (Exception e) {
            //connection = null;
            Journal.add("Не удалось создать таблицу " + tableName + e.getMessage(), NoteType.ERROR);
            return false;
        }
        return true;
    }

    public static boolean createValueTable(String tableName){
        return createValueTable(tableName, false);
    }

    public static boolean createValueTable(String tableName, boolean drop){
        return createTable(tableName, "moment TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3), value BIGINT", drop);
    }

     /**
     * Получить значение из БД по имени таблицы на текущую дату
     * @param tableName Название таблицы
     * @return Значение из БД
     */
    public static long getValue(String tableName)throws Exception{
        return getValue(tableName, new Date());
    }

    /**
     * Получить значение из БД по имени таблицы на указанную дату
     * @param tableName Название таблицы
     * @param date Дата
     * @return Значение из БД
     */
    public static long getValue(String tableName, Date date)throws Exception{
        //long defaultValue = 0;
        if(!connected()) throw new Exception("Нет подключения к базе данных");
        PreparedStatement ps = connection.prepareStatement(
                "SELECT value FROM " + tableName + " WHERE (moment <= ?) ORDER BY moment DESC LIMIT 1");
        ps.setTimestamp(1, new Timestamp(date.getTime()));
        if (ps.execute()) {
            ResultSet rs = ps.getResultSet();
            //return (rs.next() ? rs.getDouble(0) : Double.NaN);
            if (rs.next())
                return rs.getBigDecimal(1).longValue();
            else
                throw new Exception("Таблица значений пуста, впрочем как и все феномены этой жизни...");
        } else
            throw new Exception("Значение не найдено в базе данных");
    }

    /**
     * <b>Вставить значение</b> в указанную таблицу значений на указанную дату<br>
     * @param tableName Наименование таблицы<br>
     * @param value Значение<br>
     * @return Вставленное значение
     */
    public static boolean setValue(String tableName, long value){
        if(!connected())return false;
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + tableName +
                    " (value) VALUES (?)");
            ps.setLong(1, value);
            ps.executeUpdate();
            Journal.add("В таблицу " + tableName + " добавлено значение " + value);
            return true;
        } catch (SQLException e) {
            //connection = null;
            Journal.add("Ошибка записи в таблицу " + tableName + e.getMessage(),NoteType.ERROR);
            return false;
        }
    }

    public static boolean eraseTable(String tableName){
        if(!connected())return false;
        try {
            Statement statement = connection.createStatement();
            statement.execute("DELETE FROM " + tableName);
            statement.close();
            Journal.add("Таблица " + tableName + " очищена");
            return true;
        } catch (SQLException e) {
            Journal.add(e.getMessage(), NoteType.ERROR);
            return false;
        }
    }


}
