package ru.staffbots.tools.values;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Locale;


/**
 * <b>Контейнер {@code Double}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Double}.
 *
 */
public class DoubleValue extends Value{

    /**
     * <b>Минимальное значение</b> инициируется в конструкторе.
     */
    protected double minValue = Long.MIN_VALUE;

    /**
     * <b>Максимальное значение</b> инициируется в конструкторе.
     */
    protected double maxValue = Long.MAX_VALUE;

    /**
     * <b>Значение по умолчанию</b> инициируется в конструкторе.
     */
    protected double defaultValue = 0.0;

    /**
     * <b>Установить диапазон</b> значений и значение по умолчанию
     * @param minValue минимальное значение
     * @param defaultValue значение по умолчанию
     * @param maxValue максимальное значение
     */
    private void setRange(double minValue, double defaultValue, double maxValue){
        this.value = toLong(defaultValue);

        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;

    }

    private void initValues(Double... values){
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

    public DoubleValue(String name, String note, ValueMode valueMode, int precision, Double... values) {
        super(name, note, valueMode, ValueType.DOUBLE, 0);
        initValues(values);
        this.precision = precision;
    }

    public DoubleValue(String name, String note, int precision, Double... values) {
        super(name, note, ValueType.DOUBLE, 0);
        initValues(values);
        this.precision = precision;
    }

    /*******************************************************
     *****         Работа со значением                 *****
     *******************************************************/

    public void setValue(Double value){
        super.set(toLong(value));
    }

    public double getValue(){
        return getValue(new Date());
    }

    public double getValue(Date date){
        return fromLong(super.get(date));
    }

    @Override
    public void reset() {
        setValue(defaultValue);
    }

    /*******************************************************
     *****         Преобразование типов                *****
     *******************************************************/
    /**
     * <b>Точность округления</b><br>
     * количество знаков после запятой
     */
    public int precision;

    public static String toString(double value, int precision){
        return String.format(Locale.US, "%." + precision + "f", value);
    }

    public static long toLong(double value) {
        return Double.doubleToLongBits(value);
    }

    public static double fromString(String value) throws Exception{
        return Double.parseDouble(value);
    }

    public static double fromString(String value, double defaultValue){
        try {
            return fromString(value);
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    public static double fromLong(long value) {
        return Double.longBitsToDouble(value);
    }

    @Override
    public String toString(){
        return toString(getValue(), precision);
    }

    @Override
    public String toValueString(long value){
        return toString(fromLong(value), precision);
    }

    // Устанавливает значение из строки value
    @Override
    public void setFromString(String value){
        try {
               setValue(fromString(value));
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, "SetValue", name, note, value, exception.getMessage());
        }
    }

}
