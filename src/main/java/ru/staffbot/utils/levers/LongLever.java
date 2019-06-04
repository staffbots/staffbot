package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.LongValue;

public class LongLever extends LongValue implements Lever {

    public LongLever(String name, String note, Boolean dbStorage, long... values) {
        super(name, note, dbStorage, values);
    }

    public LongLever(String name, String note, long... values) {
        super(name, note, values);
    }

}
