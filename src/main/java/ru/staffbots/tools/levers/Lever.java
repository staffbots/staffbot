package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.values.ValueType;

import java.io.Serializable;

/**
 * <b>Рычаг управления</b><br>
 * должен уметь возвращать html-код визуального компонента
 * и содержать значение параметра<br>
 */
public interface Lever{

    default boolean isButton() {
        return (getType().equalsIgnoreCase("button"));
    }

    default boolean isGroup() {
        return (getType().equalsIgnoreCase("group"));
    }

    default String getType(){
        // Имя дочернего класса по идее: className = <Type>Lever
        String className = this.getClass().getSimpleName().toLowerCase();
        // Возвращаем, только <Type>
        return className.substring(0, className.length() - 5);
    }

    /**
     * Получить имя ресурса
     */
    default String getTemplateFile(){
        // Из <Type> получаем имя ресурса: levers/<Type>.html
        return "control/levers/" + getType() + ".html";
    }

    Value toValue();

    default String getName(){
        return toValue().getName();
    }

    default long set(long newValue) {
        return toValue().set(newValue);
    }

    default ValueType getValueType(){
        return toValue().getValueType();
    }

    default boolean isChangeable(){
        return toValue().isChangeable();
    }

    default boolean isStorable(){
        return toValue().isStorable();
    }

    default String getTableName() {
        return  toValue().getTableName();
    }

}
