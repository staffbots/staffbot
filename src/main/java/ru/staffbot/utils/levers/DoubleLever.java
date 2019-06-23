package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.DoubleValue;

public class DoubleLever extends DoubleValue implements Lever {

    public DoubleLever(String name, String note, Boolean dbStorage, Double... values) {
        super(name, note, dbStorage, values);
    }

    public DoubleLever(String name, String note, Double... values) {
        super(name, note, values);
    }

    public DoubleLever(String name, String note, Boolean dbStorage, LeverMode mode, Double... values) {
        super(name, note, dbStorage, values);
        this.mode = mode;
    }

    public DoubleLever(String name, String note, LeverMode mode, Double... values) {
        super(name, note, values);
        this.mode = mode;
    }

}
