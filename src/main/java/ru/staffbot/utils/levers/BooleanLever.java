package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.BooleanValue;

public class BooleanLever extends BooleanValue implements Lever {

    private static final String defaultTrueValue = "checked";
    private static final String defaultFalseValue = "";


    public BooleanLever(String name, String note, boolean value, Boolean dbStorage) {
        super(name, note, value, dbStorage);
        trueValue = defaultTrueValue;
        falseValue = defaultFalseValue;
    }

    public BooleanLever(String name, String note, boolean value) {
        super(name, note, value);
        trueValue = defaultTrueValue;
        falseValue = defaultFalseValue;
    }

    @Override
    public void setValueFromString(String value){
        setValue("on".equalsIgnoreCase(value));
    }

}
