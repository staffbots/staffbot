package ru.staffbots.tools.values;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <b>Контейнер {@code Date}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Date},
 * в которых, однако, всё сводится к {@code Double}-значению, с помощью {@link Converter}
 */
public class DateValue extends Value{

    private DateFormat format;

    public DateValue(String name, String note, ValueMode valueMode, DateFormat format, Date value) {
        super(name, note, valueMode, ValueType.DATE, toLong(value));
        this.format = format;
    }

    public DateValue(String name, String note, DateFormat format, Date value) {
        super(name, note, ValueType.DATE, toLong(value));
        this.format = format;
    }

    public void setValue(Date value){
        set(toLong(value));
    }

    public Date getValue(){
        return getValue(new Date());
    }

    public Date getValue(Date date){
        return fromLong(get(date));
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String getValueAsString(){
        return DateValue.toString(fromLong(get()), format);
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

    // Устанавливает значение из строки value
    @Override
    public void setValueFromString(String value){
        try {
            setValue(fromString(value, format));
        } catch (Exception exception) {
            Journal.add("Неудачная попытка присвоить " + name + " значение " + value,
                    NoteType.ERROR, exception);
        }
    }

    public static long toLong(Date value) {
        return value.getTime(); // Узнаём количество миллисекунд
    }

    @Override
    public long toLong() {
        return toLong(getValue());
    }

    @Override
    public void setValueFromLong(long value) {
        setValue(fromLong(value));
    }

    public static Date fromLong(long value) {
        return new Date(value); // Задаем количество миллисекунд
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

    // Конвертирует дату value в строку в формате format
    public static String toString(Date value, DateFormat format){
        return toString(value, format, "");
    }

//    @Override
//    public void setValueFromString(String value){
//        setValue(Converter.stringToDate(value,format));
//    }

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
