package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.ListValue;

public class ListLever extends ListValue implements Lever {

    public ListLever(String name, String note, Boolean dbStorage, long defaultValue, String... values) {
        super(name, note, dbStorage, defaultValue, values);
    }

    public ListLever(String name, String note, Boolean dbStorage, String... values) {
        super(name, note, dbStorage, 0, values);
    }

    public ListLever(String name, String note, String... values) {
        super(name, note, 0, values);
    }

    public ListLever(String name, String note, Boolean dbStorage, LeverMode mode, long defaultValue, String... values) {
        super(name, note, dbStorage, defaultValue, values);
        this.mode = mode;
    }

    public ListLever(String name, String note, Boolean dbStorage, LeverMode mode, String... values) {
        super(name, note, dbStorage, 0, values);
        this.mode = mode;
    }

    public ListLever(String name, String note, LeverMode mode, String... values) {
        super(name, note, 0, values);
        this.mode = mode;
    }


}
