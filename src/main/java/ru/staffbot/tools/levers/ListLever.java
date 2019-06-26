package ru.staffbot.tools.levers;

import ru.staffbot.tools.values.ListValue;
import ru.staffbot.tools.values.ValueMode;

public class ListLever extends ListValue implements Lever {

    public ListLever(String name, String note, ValueMode valueMode, long defaultValue, String... values) {
        super(name, note, valueMode, defaultValue, values);
    }

    public ListLever(String name, String note, ValueMode valueMode, String... values) {
        super(name, note, valueMode, 0, values);
    }

    public ListLever(String name, String note, String... values) {
        super(name, note, 0, values);
    }

    public ListLever(String name, String note, ValueMode valueMode, LeverMode leverMode, long defaultValue, String... values) {
        super(name, note, valueMode, defaultValue, values);
        this.leverMode = leverMode;
    }

    public ListLever(String name, String note, ValueMode valueMode, LeverMode leverMode, String... values) {
        super(name, note, valueMode, 0, values);
        this.leverMode = leverMode;
    }

    public ListLever(String name, String note, LeverMode leverMode, String... values) {
        super(name, note, 0, values);
        this.leverMode = leverMode;
    }


}
