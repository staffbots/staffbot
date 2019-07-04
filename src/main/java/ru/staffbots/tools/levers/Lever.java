package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.Value;

/**
 * <b>Рычаг управления</b><br>
 * должен уметь возвращать html-код визуального компонента
 * и содержать значение параметра<br>
 */
public interface Lever{

    default String getType(){
        // Имя дочернего класса по идее: className = <Type>Lever
        String className = this.getClass().getSimpleName().toLowerCase();
        // Возвращаем, только <Type>
        return className.substring(0, className.length() - 5);
    }
    /**
     * <b>Получить html</b>-код визуального компонента
     * @return html-код визуального компонента
     */
    default String getTemplateFile(){
        // Из <Type> получаем имя ресурса: levers/<Type>.html
        return "levers/" + getType() + ".html";
    };

    Value toValue();

}
