package ru.staffbots.tools.values;

import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;

import java.util.ArrayList;
import java.util.Date;

/**
 */
public class ListValue extends Value {

    public ArrayList<String> list = new ArrayList<>();

    /**
     * <b>Минимальное значение</b> инициируется в конструкторе.
     */
    protected long minValue = 0;

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
     * @param defaultValue значение по умолчанию
     */
    private void init(long defaultValue, String... values){
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = 0;
        this.maxValue = values.length - 1;
        for (String value : values)
            list.add(value);
    }

    public ListValue(String name, String note, ValueMode valueMode, long defaultValue, String... values) {
        super(name, note, valueMode, ValueType.LIST, defaultValue);
        init(defaultValue, values);
    }

    public ListValue(String name, String note, long defaultValue, String... values) {
        super(name, note, ValueType.LIST, defaultValue);
        init(defaultValue, values);
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
    public String toString(){
        return toString(get());
    }

    @Override
    public String toString(long value){
        String result = "";
        for (int i = 0; i < list.size(); i++)
            result += "<option value=\"" + i + "\""
                    + (i == value ? " selected" : " ") + ">"
                    + list.get(i) + "</option>";
        return result;
    }


    @Override
    public long toLong() {
        return getValue();
    }

    @Override
    public void setFromString(String value) {
        set(LongValue.fromString(value, 0));
    }

    @Override
    public void setFromLong(long value) {
        setValue(value);
    }


}
