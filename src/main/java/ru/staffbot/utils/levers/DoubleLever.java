package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.DoubleValue;

public class DoubleLever extends DoubleValue implements Lever {

    public DoubleLever(String name, String note, Boolean dbStorage, Double... values) {
        super(name, note, dbStorage, values);
    }

    public DoubleLever(String name, String note, Double... values) {
        super(name, note, values);
    }

}
