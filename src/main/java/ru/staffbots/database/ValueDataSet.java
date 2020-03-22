package ru.staffbots.database;

import java.util.Date;

public class ValueDataSet {

    public Date moment;
    public String value;

    public ValueDataSet(Date moment, String value){
        this.moment = moment;
        this.value = value;
    }

}
