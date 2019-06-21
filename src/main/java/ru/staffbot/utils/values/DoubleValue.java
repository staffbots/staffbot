package ru.staffbot.utils.values;

import ru.staffbot.utils.Converter;

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
        this.value = Converter.doubleToLong(defaultValue);
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

    public DoubleValue(String name, String note,  Boolean dbStorage, Double... values) {
        super(name, note, 0, ValueType.DOUBLE, dbStorage);
        initValues(values);
    }

    public DoubleValue(String name, String note, Double... values) {
        super(name, note, 0, ValueType.DOUBLE);
        initValues(values);
    }

    public Double getValue(){
        return getValue(new Date());
    }

    public static Double getValue(long value){
        return Converter.longToDouble(value);
    }

    public Double getValue(Date date){
        return Converter.longToDouble(super.get(date));
    }

    public void setValue(Double value){
        super.set(Converter.doubleToLong(value));
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

    @Override
    public void setValueFromString(String value){
        if(value != null)
            setValue(Double.parseDouble(value));
    }

}
