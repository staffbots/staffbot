package ru.staffbots.database;

import ru.staffbots.database.users.User;
import ru.staffbots.tools.TemplateFillable;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
//import com.mysql.jdbc.Driver;
import java.util.TimeZone;

/*
 * Перечисление СУБД,
 * пока только mySQL, а больше и не надо
 */
public enum DBMS  implements TemplateFillable {

    MySQL("com.mysql.cj.jdbc.Driver");
//    MySQL("com.mysql.jdbc.Driver");

    private String driver;

    DBMS(String driver) {
        this.driver = driver;
    }

    public String getDriver(){
        return driver;
    }

    public Connection getConnection(String server, int port, User user) throws Exception {
        return getConnection(server, port, null, user);
    }

    public Connection getConnection(String server, int port, String database, User user) throws Exception {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:" + name() + "://").
            append(server + ":").
            append(port + "/").
            append(database == null ? "?serverTimezone=UTC" : database); //Невозможно сразу установить смещение от UTC
        Connection connection = null;
        try {
            DriverManager.registerDriver((Driver) Class.forName(driver).getDeclaredConstructor().newInstance());
            connection = DriverManager.getConnection(url.toString(), user.login, user.password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (database == null) {
            Statement statement = connection.createStatement();
            statement.execute("SET GLOBAL time_zone='+" + (int) (TimeZone.getDefault().getRawOffset() / 36E5) + ":00'");
            statement.close();
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
