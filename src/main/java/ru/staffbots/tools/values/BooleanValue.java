package ru.staffbots.tools.values;


import java.util.Date;

/**
 * <b>Контейнер {@code Boolean}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Boolean},
 * в которых, однако, всё сводится к {@code Double}-значению, с помощью {@link Converter}
 */
public class BooleanValue extends Value {

    public boolean defaultValue = false;

    public String trueValue = "<input type=\"checkbox\" checked disabled>";

    public String falseValue = "<input type=\"checkbox\" disabled>";


    public BooleanValue(String name, String note, ValueMode valueMode, boolean value) {
        super(name, note, valueMode, ValueType.BOOLEAN, toLong(value));
    }

    public BooleanValue(String name, String note, boolean value) {
        super(name, note, ValueType.BOOLEAN, toLong(value));
    }

    /*******************************************************
     *****         Работа со значением                 *****
     *******************************************************/

    public Boolean setValue(Boolean value){
        return fromLong(set(toLong(value)));
    }

    public boolean getValue(){
        return getValue(new Date());
    }

    public Boolean getValue(Date date){
        return fromLong(get(date));
    }

    @Override
    public void reset() {
        setValue(defaultValue);
    }

    /*******************************************************
     *****         Преобразование типов                *****
     *******************************************************/

    public static long toLong(boolean value) {
        return (value ? 1 : 0);
    }

    public static boolean fromLong(long value) {
        return (value > 0.5);
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    // для интерфейса
    public String toString(){
        return fromLong(get()) ? trueValue : falseValue;
    }

    @Override
    // для графиков
    public String toString(long value){
        return Long.toString(toLong(fromLong(value)));
    }

    @Override
    public long toLong() {
        return toLong(getValue());
    }

    @Override
    public void setFromString(String value){
        setValue(trueValue.equalsIgnoreCase(value));
    }

    @Override
    public void setFromLong(long value) {
        setValue(fromLong(value));
    }


}
