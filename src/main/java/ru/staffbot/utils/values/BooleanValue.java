package ru.staffbot.utils.values;

import ru.staffbot.utils.Converter;

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


    public BooleanValue(String name, String note, Boolean dbStorage, boolean value) {
        super(name, note, dbStorage, ValueType.BOOLEAN, Converter.booleanToLong(value));
    }

    public BooleanValue(String name, String note, boolean value) {
        super(name, note, ValueType.BOOLEAN, Converter.booleanToLong(value));
    }

    public Boolean setValue(Boolean value){
        return Converter.longToBoolean(set(Converter.booleanToLong(value)));
    }

    public boolean getValue(){
        return getValue(new Date());
    }

    public Boolean getValue(Date date){
        return Converter.longToBoolean(get(date));
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


}
