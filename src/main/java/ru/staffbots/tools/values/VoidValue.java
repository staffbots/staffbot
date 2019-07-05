package ru.staffbots.tools.values;




/**
 * <b>Пустое значение</b> расширяет {@link Value},
 */
public class VoidValue extends Value{

    public VoidValue(String note) {
        super("", note, ValueMode.TEMPORARY, ValueType.VOID, 0);
    }

    @Override
    public void setValueFromString(String value) {
        set(LongValue.fromString(value, 0));
    }

    @Override
    public long toLong() {
        return 0;
    }

    @Override
    public void setValueFromLong(long value) {
    }

}
