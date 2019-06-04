package ru.staffbot.utils.values;


import ru.staffbot.utils.levers.EmptyLever;

/**
 * <b>Пустое значение</b> расширяет {@link Value},
 * используется в {@link EmptyLever}
 */
public class EmptyValue extends Value{

    public EmptyValue(String note) {
        super("", note, 0, ValueType.EMPTY, false);
    }

}
