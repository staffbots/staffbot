package ru.staffbots.database;

import ru.staffbots.database.users.User;
import ru.staffbots.tools.TemplateFillable;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Map;
import java.util.TimeZone;

/*
 * Перечисление СУБД,
 * пока только mySQL, а больше и не надо
 */
public enum DBMS  implements TemplateFillable {

    MySQL("com.mysql.cj.jdbc.Driver");

    private String driver;

    DBMS(String driver) {
        this.driver = driver;
    }

    public String getDriver(){
        return driver;
    }

    public Connection getConnection(String server, int port, User user) throws Exception {
        return getConnection(server, port, user, null);
    }

    public Connection getConnection(String server, int port, User user, String database) throws Exception {
        Connection connection;
        switch (this) {
            case MySQL:
                DriverManager.registerDriver((Driver) Class.forName(driver).getDeclaredConstructor().newInstance());
                String url = "jdbc:mysql://" + server + ":" + port + "/" +
                        ((database == null) ? "?serverTimezone=UTC" : database);
                //Невозможно сразу установить смещение от UTC
                connection = DriverManager.getConnection(url, user.login, user.password);
                if (database == null)
                    connection.createStatement().execute(
                        "SET GLOBAL time_zone='+" + (int) (TimeZone.getDefault().getRawOffset() / 36E5) + ":00'");
                break;
            default:
                throw new Exception("No " + this + " driver");
        }
        return connection;
    }

    public static DBMS getByName(String name){
        for (DBMS value : DBMS.values())
            if (value.name().equalsIgnoreCase(name))
                return value;
        return null;
    }

}
