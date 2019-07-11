package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

public class DoubleLever extends DoubleValue implements Lever {

    public DoubleLever(String name, String note, ValueMode valueMode, int precision, Double... values) {
        super(name, note, valueMode, precision, values);
    }

    public DoubleLever(String name, String note, int precision, Double... values) {
        super(name, note, precision, values);
    }

    public DoubleLever(String name, String note, ValueMode valueMode, LeverMode leverMode, int precision, Double... values) {
        super(name, note, valueMode, precision, values);
        this.leverMode = leverMode;
    }

    public DoubleLever(String name, String note, LeverMode leverMode, int accuracy, Double... values) {
        super(name, note, accuracy, values);
        this.leverMode = leverMode;
    }

}
