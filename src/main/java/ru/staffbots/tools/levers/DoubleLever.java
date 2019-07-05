package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

public class DoubleLever extends DoubleValue implements Lever {

    public DoubleLever(String name, String note, ValueMode valueMode, int accuracy, Double... values) {
        super(name, note, valueMode, accuracy, values);
    }

    public DoubleLever(String name, String note, int accuracy, Double... values) {
        super(name, note, accuracy, values);
    }

    public DoubleLever(String name, String note, ValueMode valueMode, LeverMode leverMode, int accuracy, Double... values) {
        super(name, note, valueMode, accuracy, values);
        this.leverMode = leverMode;
    }

    public DoubleLever(String name, String note, LeverMode leverMode, int accuracy, Double... values) {
        super(name, note, accuracy, values);
        this.leverMode = leverMode;
    }

}
