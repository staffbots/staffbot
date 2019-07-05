package ru.staffbots.tools.values;




/**
 * <b>Пустое значение</b> расширяет {@link Value},
 */
public class VoidValue extends Value{

    public VoidValue(String note) {
        super("", note, ValueMode.TEMPORARY, ValueType.VOID, 0);
    }

    /*******************************************************
     *****         Работа со значением                 *****
     *******************************************************/

    @Override
    public void reset() {
    }

    /*******************************************************
     *****         Преобразование типов                *****
     *******************************************************/

    @Override
    public String toString() {
        return null;
    }

    @Override
    public String toString(long value) {
        return null;
    }

    @Override
    public long toLong() {
        return 0;
    }

    @Override
    public void setFromString(String value) {
    }

    @Override
    public void setFromLong(long value) {
    }

}
