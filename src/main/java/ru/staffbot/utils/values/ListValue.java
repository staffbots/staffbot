package ru.staffbot.utils.values;

import ru.staffbot.utils.Converter;

import java.util.ArrayList;
import java.util.Date;

/**
 * <b>Контейнер {@code Long}-значения</b> расширяет {@link Value},
 * предоставляя методы работы со значениями типа {@code Long},
 * в которых, однако, всё сводится к {@code Double}-значению, с помощью {@link Converter}
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
     * @param minValue минимальное значение
     * @param defaultValue значение по умолчанию
     * @param maxValue максимальное значение
     */
    private void init(long defaultValue, String... values){
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue = 0;
        this.maxValue = values.length - 1;
        for (String value : values)
            list.add(value);
    }


    public ListValue(String name, String note, long defaultValue, Boolean dbStorage, String... values) {
        super(name, note, defaultValue, ValueType.LIST, dbStorage);
        init(defaultValue, values);
    }

    public ListValue(String name, String note, long defaultValue, String... values) {
        super(name, note, defaultValue, ValueType.LIST);
        init(defaultValue, values);
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
        String result = "";
        for (int i = 0; i < list.size(); i++)
            result += "<option value=\"" + i + "\""
                    + (i == value ? " selected" : " ") + ">"
                    + list.get(i) + "</option>";
        return result;
    }


}
