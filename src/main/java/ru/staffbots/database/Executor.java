package ru.staffbots.database;

import java.sql.ResultSet;
import java.sql.Statement;

public class Executor<T> {

    public T execQuery(String query, ResultHandler<T> handler) throws Exception {
        if(Database.disconnected()) throw Database.getException();
        Statement statement = Database.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        T result = handler.handle(resultSet);
        resultSet.close();
        statement.close();
        return result;
    }

    public int execUpdate(String update) throws Exception {
        if(Database.disconnected()) throw Database.getException();
        Statement statement = Database.getConnection().createStatement();
        int result = statement.executeUpdate(update);
        statement.close();
        return result;
    }

}
