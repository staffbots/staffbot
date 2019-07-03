package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.ValueMode;
import ru.staffbots.tools.values.ValueType;
import ru.staffbots.tools.values.VoidValue;

public class ButtonLever extends VoidValue implements Lever {

    private String caption;

    private Runnable actionOnClick;

    public ButtonLever(String name, String caption, String note, Runnable actionOnClick) {
        super(note);
        this.name = name;
        this.caption = caption;
        this.valueMode = ValueMode.TEMPORARY;
        this.actionOnClick = actionOnClick;
    }

    @Override
    public String getValueAsString(){
        return caption;
    }

    public void onClick(){
        actionOnClick.run();
    }
}
