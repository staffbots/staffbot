package ru.staffbot.utils.values;




/**
 * <b>Пустое значение</b> расширяет {@link Value},
 * используется в {@link VoidLever}
 */
public class VoidValue extends Value{

    public VoidValue(String note) {
        super("", note, 0, ValueType.VOID, false);
    }

}
