package ru.staffbots.tools.values;




/**
 * <b>Пустое значение</b> расширяет {@link Value},
 */
public class VoidValue extends Value{

    public VoidValue(String note) {
        super("", note, ValueMode.TEMPORARY, ValueType.VOID, 0);
    }

}
