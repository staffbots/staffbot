package ru.staffbots.tools.values;


import java.util.Date;

/**
 * <b>Контейнер {@code Boolean}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Boolean},
 * в которых, однако, всё сводится к {@code Double}-значению, с помощью {@link Converter}
 */
public class BooleanValue extends Value {

//    public String trueValue = "true";
//    public String falseValue = "false";

    public String trueValue = "<input type=\"checkbox\" checked disabled>";
    public String falseValue = "<input type=\"checkbox\" disabled>";


    public BooleanValue(String name, String note, ValueMode valueMode, boolean value) {
        super(name, note, valueMode, ValueType.BOOLEAN, toLong(value));
    }

    public BooleanValue(String name, String note, boolean value) {
        super(name, note, ValueType.BOOLEAN, toLong(value));
    }

    public Boolean setValue(Boolean value){
        return fromLong(set(toLong(value)));
    }

    public boolean getValue(){
        return getValue(new Date());
    }

    public Boolean getValue(Date date){
        return fromLong(get(date));
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String getValueAsString(){
        return get() == 1 ? trueValue : falseValue;
    }

    @Override
    public void setValueFromString(String value){
        setValue(trueValue.equalsIgnoreCase(value));
    }

    @Override
    public long toLong() {
        return toLong(getValue());
    }

    public static long toLong(boolean value) {
        return (value ? 1 : 0);
    }

    @Override
    public void setValueFromLong(long value) {
        setValue(fromLong(value));
    }

    public static boolean fromLong(long value) {
        return (value > 0.5);
    }

}
