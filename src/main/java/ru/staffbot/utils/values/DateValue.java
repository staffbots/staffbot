package ru.staffbot.utils.values;

import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.DateScale;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * <b>Контейнер {@code Date}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Date},
 * в которых, однако, всё сводится к {@code Double}-значению, с помощью {@link Converter}
 */
public class DateValue extends Value{

    private DateFormat format;

    public DateValue(String name, String note, ValueMode valueMode, DateFormat format, Date value) {
        super(name, note, valueMode, ValueType.DATE, Converter.dateToLong(value));
        this.format = format;
    }

    public DateValue(String name, String note, DateFormat format, Date value) {
        super(name, note, ValueType.DATE, Converter.dateToLong(value));
        this.format = format;
    }

    public void setValue(Date value){
        set(Converter.dateToLong(value));
    }

    public Date getValue(){
        return getValue(new Date());
    }

    public Date getValue(Date date){
        return Converter.longToDate(get(date));
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String getValueAsString(){
        return Converter.dateToString(Converter.longToDate(get()), format);
    }

    @Override
    public void setValueFromString(String value){
        setValue(Converter.stringToDate(value,format));
    }

    /*
     * Получить ближайший момент времени, у которого часы и минуты
     * совпадают с текущим значением времени
     *
     */
    public Date getNearFuture(){
        return getNearFuture(getValue());
    }

    public static Date getNearFuture(Date date){
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(date);
        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
        resultCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
        resultCalendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));
        resultCalendar.set(Calendar.MILLISECOND, currentCalendar.get(Calendar.MILLISECOND));
        if (resultCalendar.getTime().before(new Date()))
            resultCalendar.add(Calendar.DAY_OF_MONTH, 1);
        return resultCalendar.getTime();
    }

}
