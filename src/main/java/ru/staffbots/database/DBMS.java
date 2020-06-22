package ru.staffbots.database;

import java.sql.*;
import java.util.TimeZone;

/*
 * Перечисление СУБД,
 * пока только mySQL, а больше и не надо
 */
public enum DBMS {

    MySQL("com.mysql.cj.jdbc.Driver");

    private String driver;

    DBMS(String driver) {
        this.driver = driver;
    }

    public Connection getConnection(String server, int port, String login, String password) throws Exception {
        return getConnection(server, port, null, login, password);
    }

    public Connection getConnection(String server, int port, String database, String login, String password) throws Exception {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:" + name() + "://").
            append(server + ":").
            append(port + "/").
            append(database == null ? "?serverTimezone=UTC" : database); //Невозможно сразу установить смещение от UTC
        DriverManager.registerDriver((Driver) Class.forName(driver).getDeclaredConstructor().newInstance());
        Connection connection = DriverManager.getConnection(url.toString(), login, password);
        if (database == null) {
            Statement statement = connection.createStatement();
            statement.execute("SET GLOBAL time_zone='+" + (int) (TimeZone.getDefault().getRawOffset() / 36E5) + ":00'");
            statement.close();
        }
        return connection;
    }

}
