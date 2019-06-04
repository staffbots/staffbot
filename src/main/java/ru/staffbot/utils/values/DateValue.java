package ru.staffbot.utils.values;

import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;

import java.util.Date;

/**
 * <b>Контейнер {@code Date}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Date},
 * в которых, однако, всё сводится к {@code Double}-значению, с помощью {@link Converter}
 */
public class DateValue extends Value{

    private DateFormat format;

    public DateValue(String name, String note, Date value, DateFormat format, Boolean dbStorage) {
        super(name, note, Converter.dateToLong(value), ValueType.DATE, dbStorage);
        this.format = format;
    }

    public DateValue(String name, String note, Date value, DateFormat format) {
        super(name, note, Converter.dateToLong(value), ValueType.DATE);
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

}
