package ru.staffbot.utils.levers;

import ru.staffbot.database.DBTable;
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

    Value toValue();
}
