package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.BooleanValue;

public class BooleanLever extends BooleanValue implements Lever {

    public BooleanLever(String name, String note, boolean value, Boolean dbStorage) {
        super(name, note, value, dbStorage);
    }

    public BooleanLever(String name, String note, boolean value) {
        super(name, note, value);
    }

    @Override
    public void setValueFromString(String value){
        String stringValue = "on".equalsIgnoreCase(value) ? trueValue : falseValue;
        setValue(trueValue.equalsIgnoreCase(stringValue));
    }

//    @Override
//    public String getValueAsString(){
//        return (get() == 1) ? "checked" : "";
//    }

}
