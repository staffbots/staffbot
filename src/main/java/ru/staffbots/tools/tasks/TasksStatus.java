package ru.staffbots.tools.tasks;

import ru.staffbots.tools.Translator;

import java.util.HashMap;
import java.util.Map;

public enum TasksStatus {

    START,
    PAUSE,
    STOP;

    public String getDescription(){
        return Translator.getValue("tasksstatus", getName());
    }

    public String getName() {
        return name().toLowerCase();
    }

}
