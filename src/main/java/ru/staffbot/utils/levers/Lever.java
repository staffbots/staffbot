package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.Value;
import ru.staffbot.utils.values.ValueType;

/**
 * <b>Рычаг управления</b><br>
 * должен уметь возвращать html-код визуального компонента
 * и содержать значение параметра<br>
 */
public interface Lever{

    /**
     * <b>Получить html</b>-код визуального компонента
     * @return html-код визуального компонента
     */
    default String getTemplateFile(){
        String className = this.getClass().getSimpleName().toLowerCase();
        return "levers/" + className.substring(0, className.length() - 5) + ".html";
    };



    /**
     * <b>Получить</b> значение<br>
     * Во всех классах из пакета {@link ru.staffbot.utils.levers} реализующих интеофейс {@code Lever}
     * данный метод автоматически реализуется в родительском классе {@link Value}<br>
     * @return значение
     */
    long get();

    /**
     * <b>Установить</b> значение<br>
     * Во всех классах из пакета {@link ru.staffbot.utils.levers} реализующих интеофейс {@code Lever}
     * данный метод автоматически реализуется в родительском классе {@link Value}<br>
     * @param newValue - устанавлевоемое значение
     * @return установленное значение
     */
    long set(long newValue);

    /**
     * <b>Сбросить</b> значение на значение по умолчанию ({@code defaultValue})<br>
     * Во всех классах из пакета {@link ru.staffbot.utils.levers} реализующих интеофейс {@code Lever}
     * данный метод автоматически реализуется в родительском классе {@link Value}<br>
     */
    void reset();

    /**
     * Дублёр {@code ru.staffbot.utils.values.Value.getName()}
     */
    String getName();

    /**
     * Дублёр {@link Value ){@code Value.getNote()}
     */
    String getNote();

    String getValueAsString();

    void setValueFromString(String value);

    int getStringValueSize();

    boolean getDbStorage();

    ValueType getValueType();
}
