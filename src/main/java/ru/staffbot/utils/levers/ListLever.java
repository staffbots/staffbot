package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.ListValue;

public class ListLever extends ListValue implements Lever {

    public ListLever(String name, String note, long defaultValue, Boolean dbStorage, String... values) {
        super(name, note, defaultValue, dbStorage, values);
    }

    public ListLever(String name, String note, Boolean dbStorage, String... values) {
        super(name, note, 0, dbStorage, values);
    }

    public ListLever(String name, String note, String... values) {
        super(name, note, 0, values);
    }

}
