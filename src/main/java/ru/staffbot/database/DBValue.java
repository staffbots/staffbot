package ru.staffbot.database;

import java.util.Date;

public class DBValue {
    public Date moment;
    public String value;

    public DBValue(Date moment, String value){
        this.moment = moment;
        this.value = value;
    }
}
