package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.DoubleValue;
import ru.staffbot.utils.values.ValueMode;

public class DoubleLever extends DoubleValue implements Lever {

    public DoubleLever(String name, String note, ValueMode valueMode, Double... values) {
        super(name, note, valueMode, values);
    }

    public DoubleLever(String name, String note, Double... values) {
        super(name, note, values);
    }

    public DoubleLever(String name, String note, ValueMode valueMode, LeverMode leverMode, Double... values) {
        super(name, note, valueMode, values);
        this.leverMode = leverMode;
    }

    public DoubleLever(String name, String note, LeverMode leverMode, Double... values) {
        super(name, note, values);
        this.leverMode = leverMode;
    }

}
