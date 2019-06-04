package ru.staffbot.utils.values;

import ru.staffbot.utils.Converter;

import java.util.Date;

/**
 * <b>Контейнер {@code Long}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Long},
 * в которых, однако, всё сводится к {@code Double}-значению, с помощью {@link Converter}
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

    public LongValue(String name, String note, Boolean dbStorage, long... values) {
        super(name, note, 0, ValueType.LONG, dbStorage);
        initValues(values);
    }

    public LongValue(String name, String note, long... values) {
        super(name, note, 0, ValueType.LONG);
        initValues(values);
    }

    public void setValue(long value){
        set(value);
    }

    public long getValue(){
        return getValue(new Date());
    }

    public long getValue(Date date){
        return get(date);
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    public String getValueAsString(){
        return Long.toString(getValue());
    }

}
