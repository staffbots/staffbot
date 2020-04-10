package ru.staffbots.database;

import java.sql.ResultSet;

public interface ResultHandler<T> {

    T handle(ResultSet resultSet) throws Exception;

}
