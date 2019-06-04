package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.EmptyValue;

public class LabelLever extends EmptyValue implements Lever {

    private String stringValue;

    public LabelLever(String stringValue, String note) {
        super(note);
        this.stringValue = stringValue;
    }

    @Override
    public String getValueAsString(){
        return stringValue;
    }


    @Override
    public void setValueFromString(String value){
        stringValue = value;
    }

}
