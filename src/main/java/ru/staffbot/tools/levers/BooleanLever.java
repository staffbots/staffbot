package ru.staffbot.tools.levers;

import ru.staffbot.tools.values.BooleanValue;
import ru.staffbot.tools.values.ValueMode;

public class BooleanLever extends BooleanValue implements Lever {

    public BooleanLever(String name, String note, ValueMode valueMode, LeverMode leverMode, boolean value) {
        super(name, note, valueMode, value);
        this.leverMode = leverMode;
    }

    public BooleanLever(String name, String note, ValueMode valueMode, boolean value) {
        super(name, note, valueMode, value);
    }

    public BooleanLever(String name, String note, LeverMode leverMode, boolean value) {
        super(name, note, value);
        this.leverMode = leverMode;
    }

    public BooleanLever(String name, String note, boolean value) {
        super(name, note, value);
    }

    @Override
    public void setValueFromString(String value){
        String stringValue = "on".equalsIgnoreCase(value) ? trueValue : falseValue;
        setValue(trueValue.equalsIgnoreCase(stringValue));
    }

}
