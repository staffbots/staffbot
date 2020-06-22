package ru.staffbots.tools.values;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 */
public class LongValue extends Value {

    /**
     * <b>Минимальное значение</b> инициируется в конструкторе.
     */
    protected long minValue = Long.MIN_VALUE;

    /**
     * <b>Максимальное значение</b> инициируется в конструкторе.
     */
    protected long maxValue = Long.MAX_VALUE;

    /**
     * <b>Значение по умолчанию</b> инициируется в конструкторе.
     */
    protected long defaultValue = 0;

    /**
     * <b>Установить диапазон</b> значений и значение по умолчанию
     * @param minValue минимальное значение
     * @param defaultValue значение по умолчанию
     * @param maxValue максимальное значение
     */
    private void setRange(long minValue, long defaultValue, long maxValue){
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    private void initValues(long... values){
        switch (values.length){
            case 0: {
                setRange( Long.MIN_VALUE, 0, Long.MAX_VALUE);
                break;
            }
            case 1: {
                setRange( Long.MIN_VALUE, values[0], Long.MAX_VALUE);
                break;
            }
            case 2: {
                setRange( values[0], 0, values[1]);
                break;
            }
            default: setRange( values[0], values[1], values[values.length - 1]);
        }
    }

    public LongValue(String name, String note, ValueMode valueMode, long... values) {
        super(name, note, valueMode, ValueType.LONG, 0);
        initValues(values);
    }

    public LongValue(String name, String note, long... values) {
        super(name, note, ValueType.LONG, 0);
        initValues(values);
    }

    /*******************************************************
     *****         Работа со значением                 *****
     *******************************************************/

    public void setValue(long value){
        if (value < minValue) value = minValue;
        if (value > maxValue) value = maxValue;
        set(value);
    }

    public long getValue(){
        return getValue(new Date());
    }

    public long getValue(Date date){
        return get(date);
    }

    @Override
    public void reset() {
        setValue(defaultValue);
    }

    /*******************************************************
     *****         Преобразование типов                *****
     *******************************************************/

    public static boolean isLong(String value){
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static long fromString(String value) throws Exception{
        return Long.parseLong(value);
    }

    public static long fromString(String value, long defaultValue){
        try {
            return fromString(value);
        } catch (Exception exception) {
            return defaultValue;
        }
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
        return Long.toString(value);
    }

    // Устанавливает значение из строки value
    @Override
    public void setFromString(String value){
        try {
            set(LongValue.fromString(value));
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "set_value", name, note, value, exception.getMessage());
        }
    }

}
