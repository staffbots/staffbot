package ru.staffbot.utils.values;




/**
 * <b>Пустое значение</b> расширяет {@link Value},
 */
public class VoidValue extends Value{

    public VoidValue(String note) {
        super("", note, false, ValueType.VOID, 0);
    }

}
