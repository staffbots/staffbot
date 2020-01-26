package ru.staffbots.tools.values;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 */
public class DateValue extends Value{

    private Date defaultValue = null;

    private DateFormat format;

    public DateValue(String name, String note, ValueMode valueMode, DateFormat format, Date value) {
        super(name, note, valueMode, ValueType.DATE, toLong(value));
        this.format = format;
    }

    public DateValue(String name, String note, DateFormat format, Date value) {
        super(name, note, ValueType.DATE, toLong(value));
        this.format = format;
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

    /*******************************************************
     *****         Работа со значением                 *****
     *******************************************************/

    public void setValue(Date value){
        set(toLong(value));
    }

    public Date getValue(){
        return getValue(new Date());
    }

    public Date getValue(Date date){
        return fromLong(get(date));
    }

    @Override
    public void reset() {
        setValue((defaultValue == null) ? new Date() : defaultValue);
    }

    /*******************************************************
     *****         Преобразование типов                *****
     *******************************************************/

    // Конвертирует дату value в строку в формате format
    public static String toString(Date value, DateFormat format){
        return toString(value, format, "");
    }

    // Конвертирует дату value в строку в формате format
    public static String toString(Date value, DateFormat format, String defaultValue){
        try {
            SimpleDateFormat simpleFormat = new SimpleDateFormat();
            simpleFormat.applyPattern(format.get());
            return simpleFormat.format(value);
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    public static long toLong(Date value) {
        return value.getTime(); // Узнаём количество миллисекунд
    }

    // Конвертирует строку value в формате format в дату
    public static Date fromString(String value, DateFormat format) throws Exception{
        SimpleDateFormat simpleFormat = new SimpleDateFormat();
        simpleFormat.applyPattern(format.get());
        return simpleFormat.parse(value);
    }

    // Конвертирует строку value в формате format в дату
    public static Date fromString(String value, DateFormat format, Date defaultDate){
        try {
            return fromString(value, format);
        } catch (Exception exception) {
            return defaultDate;
        }
    }

    public static Date fromLong(long value) {
        return new Date(value); // Задаем количество миллисекунд
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String toString(){
        return toValueString(get());
    }

    @Override
    public String toValueString(long value){
        return toString(fromLong(value), format);
    }

    // Устанавливает значение из строки value
    @Override
    public void setFromString(String value){
        try {
            setValue(fromString(value, format));
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "set_value", getName(), note, value, exception.getMessage());
        }
    }

}
