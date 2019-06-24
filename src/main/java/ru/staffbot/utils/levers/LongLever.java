package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.LongValue;
import ru.staffbot.utils.values.ValueMode;

public class LongLever extends LongValue implements Lever {


    public LongLever(String name, String note, ValueMode valueMode, long... values) {
        super(name, note, valueMode, values);
    }

    public LongLever(String name, String note, long... values) {
        super(name, note, values);
    }

    public LongLever(String name, String note, ValueMode valueMode, LeverMode leverMode, long... values) {
        super(name, note, valueMode, values);
        this.leverMode = leverMode;
    }

    public LongLever(String name, String note, LeverMode leverMode, long... values) {
        super(name, note, values);
        this.leverMode = leverMode;
    }

}
