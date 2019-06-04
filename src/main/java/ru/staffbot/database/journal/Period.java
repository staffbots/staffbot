package ru.staffbot.database.journal;

import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.values.DateValue;

import java.util.Date;

public class Period {

    public DateFormat dateFormat;

    public DateValue fromDate;
    public DateValue toDate;

    public Period(DateFormat dateFormat){
        this.dateFormat = dateFormat;
        set((String)null, (String)null);
    }

    public Period(DateFormat dateFormat, String fromDate, String toDate){
        this.dateFormat = dateFormat;
        set(fromDate, toDate);
    }

    public Period(DateFormat dateFormat, Date fromDate, Date toDate){
        this.dateFormat = dateFormat;
        set(fromDate, toDate);
    }

    public void set(String fromDate, String toDate){
        Date defaultFromDate = new Date();
        Date defaultToDate = new Date(defaultFromDate.getTime() - dateFormat.accuracy.getNext().getValue());
        set(
                Converter.stringToDate(fromDate, dateFormat, defaultFromDate),
                Converter.stringToDate(toDate, dateFormat, defaultToDate));
    }

    public void set(Date fromDate, Date toDate){
        if (fromDate.after(toDate)){
            Date date = fromDate;
            fromDate = toDate;
            toDate = date;
        }
        if (this.fromDate == null)
            this.fromDate = new DateValue("journal_fromDate","",fromDate, dateFormat,false);
        else this.fromDate.setValue(fromDate);

        if (this.toDate == null)
            this.toDate = new DateValue("journal_todate","", toDate, dateFormat,false);
        else this.toDate.setValue(toDate);
    }

}
