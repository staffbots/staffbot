package ru.staffbots.tools.values;


import java.util.Date;

/**
 * <b>Контейнер {@code Boolean}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Boolean},
 */
public class BooleanValue extends Value {

    public boolean defaultValue = false;

    public BooleanValue(String name, String note, ValueMode valueMode, boolean value) {
        super(name, note, valueMode, ValueType.BOOLEAN, toLong(value));
    }

    public BooleanValue(String name, String note, boolean value) {
        super(name, note, ValueType.BOOLEAN, toLong(value));
    }

    /*******************************************************
     *****         Работа со значением                 *****
     *******************************************************/

    public boolean setValue(Boolean value){
        return fromLong(set(toLong(value)));
    }

    public boolean getValue(){
        return getValue(new Date());
    }

    public boolean getValue(Date date){
        return fromLong(get(date));
    }

    @Override
    public void reset() {
        setValue(defaultValue);
    }

    /*******************************************************
     *****         Преобразование типов                *****
     *******************************************************/
    // Строка для получения true-значения из html-формата
    public static final String trueValueString = "on";

    // Строка для получения false-значения из html-формата
    public static final String falseValueString = "off";

    // Строка для получения true-значения из html-формата
    public static final String trueHtmlString = "checked";

    // Строка для получения false-значения из html-формата
    public static final String falseHtmlString = "";

    // Строка для представления true-значения в html-формате
    public static final String trueViewString = "<input type='checkbox' checked disabled>";

    // Строка для представления false-значения в html-формате
    public static final String falseViewString = "<input type='checkbox' disabled>";

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
        return getValue() ? trueValueString : falseValueString;
    }

    @Override
    // для интерфейса
    public String toViewString(){
        return getValue() ? trueViewString : falseViewString;
    }

    @Override
    // для интерфейса
    public String toHtmlString(){
        return getValue() ? trueHtmlString  : falseHtmlString ;
    }

    @Override
    // для графиков
    public String toValueString(long value){
        return Long.toString(toLong(fromLong(value)));
    }

    @Override
    public void setFromString(String value){
        if (value == null) value = falseValueString;
        setValue(trueValueString.equalsIgnoreCase(value));
    }

}
