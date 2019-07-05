package ru.staffbots.tools.values;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;

import java.nio.ByteBuffer;
import java.util.Date;


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

    public DoubleValue(String name, String note,  ValueMode valueMode, Double... values) {
        super(name, note, valueMode, ValueType.DOUBLE, 0);
        initValues(values);
    }

    public DoubleValue(String name, String note, Double... values) {
        super(name, note, ValueType.DOUBLE, 0);
        initValues(values);
    }

    public Double getValue(){
        return getValue(new Date());
    }

    public static Double getValue(long value){
        return fromLong(value);
    }

    public Double getValue(Date date){
        return fromLong(super.get(date));
    }

    public void setValue(Double value){
        super.set(toLong(value));
    }

    /**
     * <b>Точность округления</b><br>
     * количество знаков после запятой
     */
    public int accuracy = 3;

    public double round(double value){
        double powerOfTen = Math.round(Math.exp(accuracy * Math.log(10)));
        return Math.round(value * powerOfTen) / powerOfTen;
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String getValueAsString(){
        return Double.toString(round(getValue()));
    }

    @Override
    public String getValueAsString(long value){
        return Double.toString(round(getValue(value)));
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

    // Устанавливает значение из строки value
    @Override
    public void setValueFromString(String value){
        try {
               setValue(fromString(value));
        } catch (Exception exception) {
            Journal.add("Неудачная попытка присвоить " + name + " значение " + value,
                    NoteType.ERROR, exception);
        }
    }

    private static byte[] doubleToBytes(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    private static long bytesToLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static long toLong(double value) {
        return bytesToLong(doubleToBytes(value));
    }

    @Override
    public long toLong(){
        return toLong(getValue());
    }

    private static byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);
        return bytes;
    }

    private static double bytesToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static double fromLong(long value) {
        return bytesToDouble(longToBytes(value));
    }

    @Override
    public void setValueFromLong(long value) {
        setValue(fromLong(value));
    }

}
