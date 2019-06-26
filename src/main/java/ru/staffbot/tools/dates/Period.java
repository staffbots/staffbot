package ru.staffbot.tools.dates;

import ru.staffbot.tools.Converter;

import java.util.Calendar;
import java.util.Date;

public class Period {

    public DateFormat dateFormat;

    public Date fromDate;

    public Date toDate;

    public String getFromDateAsString(){
        return Converter.dateToString(fromDate, dateFormat);
    }

    public String getToDateAsString(){
        return Converter.dateToString(toDate, dateFormat);
    }

    public Period(DateFormat dateFormat){
        this.dateFormat = dateFormat;
        set();
    }

    public Period(DateFormat dateFormat, String fromDate, String toDate){
        this.dateFormat = dateFormat;
        set(fromDate, toDate);
    }

    public Period(DateFormat dateFormat, Date fromDate, Date toDate){
        this.dateFormat = dateFormat;
        set(fromDate, toDate);
    }

    public void set(){
        fromDate = null;
        toDate = null;
    }


    public void set(String fromDate, String toDate){
        set(Converter.stringToDate(fromDate, dateFormat, null),
            Converter.stringToDate(toDate, dateFormat, null));
    }

    public void set(Date fromDate, Date toDate){
        if ((fromDate != null) && (toDate != null))
        if (fromDate.after(toDate)){
            Date date = fromDate;
            fromDate = toDate;
            toDate = date;
        }
        this.fromDate = fromDate;
        this.toDate  = toDate;
    }

    public void initFromDate(){
        Date date = (toDate == null) ? new Date() : toDate;
        fromDate = atStartOfDay(date);
    }

    public void initToDate(){
        Date date = (fromDate == null) ? new Date() : fromDate;
        toDate = atEndOfDay(date);
    }

    public Date atEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public Date atStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
