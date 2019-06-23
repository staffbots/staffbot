package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.BooleanValue;

public class BooleanLever extends BooleanValue implements Lever {

    public BooleanLever(String name, String note, boolean dbStorage, LeverMode mode, boolean value) {
        super(name, note, dbStorage, value);
        this.mode = mode;
    }

    public BooleanLever(String name, String note, boolean dbStorage, boolean value) {
        super(name, note, dbStorage, value);
    }

    public BooleanLever(String name, String note, LeverMode mode, boolean value) {
        super(name, note, value);
        this.mode = mode;
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
