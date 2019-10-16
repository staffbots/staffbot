package ru.staffbots.tools.dates;

import ru.staffbots.tools.values.DateValue;

import java.util.Calendar;
import java.util.Date;

public class Period {

    public DateFormat dateFormat;

    private Date fromDate;

    public Date getFromDate(){
        return fromDate;
    }

    public String getFromDateAsString(){
        return DateValue.toString(fromDate, dateFormat);
    }

    private Date toDate;

    public Date getToDate(){
        return toDate;
    }

    public String getToDateAsString(){
        return DateValue.toString(toDate, dateFormat);
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
        set(DateValue.fromString(fromDate, dateFormat, null),
            DateValue.fromString(toDate, dateFormat, null));
    }

    public void set(Date fromDate, Date toDate){
        if ((fromDate != null) && (toDate != null))
        if (fromDate.after(toDate)){
            Date date = fromDate;
            fromDate = toDate;
            toDate = date;
        }
        this.fromDate = getRoundDate(fromDate, dateFormat.dateAccuracy, false);
        this.toDate  = getRoundDate(toDate, dateFormat.dateAccuracy, true);
    }

    public void initFromDate(){
        fromDate = getRoundDate((toDate == null) ? new Date() : toDate, DateAccuracy.DAY, false);
    }

    public void initToDate(){
        toDate = getRoundDate((fromDate == null) ? new Date() : fromDate, DateAccuracy.DAY, true);
    }

    public long getDuration(){
        return toDate.getTime() - fromDate.getTime();
    }

    static private Date getRoundDate(Date date, DateAccuracy dateAccuracy, boolean roundingUp){
        if (date == null) return null;
        // Из-за часовых поясов приходиться писать такой костыль:
        if (dateAccuracy == DateAccuracy.DAY) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long time = calendar.getTime().getTime(); // Начало суток
            if (roundingUp) time += (dateAccuracy.getMilliseconds() - 1); // Конец суток
            return new Date(time);
        }
        // Хотя, если бы getTime() не вела отсчёт с привязкой к UTC+0, то всё должно было бы работать по общей формуле:
        long roundingDownTime = date.getTime() - date.getTime() % dateAccuracy.getMilliseconds(); // Начало периода в рамках dateAccuracy
        return new Date(roundingDownTime + (roundingUp ? dateAccuracy.getMilliseconds() - 1 : 0 )); // Конец периода в рамках dateAccuracy
    }


}
