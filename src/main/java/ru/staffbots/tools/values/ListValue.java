package ru.staffbots.tools.values;

import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;

import java.util.ArrayList;
import java.util.Date;

/**
 */
public class ListValue extends LongValue {

    public ArrayList<String> list = new ArrayList<>();

    /**
     * <b>Установить диапазон</b> значений и значение по умолчанию
     */
    private void init(String... values){
        valueType = ValueType.LIST;
        for (String value : values)
            list.add(value);
    }

    public ListValue(String name, String note, ValueMode valueMode, long defaultValue, String... values) {
        super(name, note, valueMode, 0 , defaultValue, values.length - 1);
        init(values);
    }

    public ListValue(String name, String note, long defaultValue, String... values) {
        super(name, note, 0 , defaultValue, values.length - 1);
        init(values);
    }

    @Override
    public int getStringValueSize(){
        int maxSize = 0;
        for (String string: list)
            if (string.length() > maxSize) maxSize = string.length();
        return maxSize;
    };

    /*******************************************************
     *****         Работа со значением                 *****
     *******************************************************/

    public void setValue(long value){
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

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    // для закладки "Управление"
    public String toString(){
        String result = "";
        for (int i = 0; i < list.size(); i++)
            result += "<option value=\"" + i + "\""
                    + (i == get() ? " selected" : " ") + ">"
                    + list.get(i) + "</option>";
        return result;
       // return toString(get());
    }

    @Override
    // для закладки "Статус"
    public  String toViewString(){
        return list.get((int)get());
    }

    @Override
    public void setFromString(String value) {
        set(LongValue.fromString(value, 0));
    }

}
