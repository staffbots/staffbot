package ru.staffbots.tools.tasks;

import ru.staffbots.tools.Translator;

import java.util.HashMap;
import java.util.Map;

public enum TasksStatus {

    START(0),
    PAUSE(1),
    STOP(2);

    private int value;
    private static Map map = new HashMap<>();

    TasksStatus(int value) {
        this.value = value;
    }

    public String getDescription(){
        return Translator.getValue("tasksstatus", getName());
    }

    static {
        for (TasksStatus status : TasksStatus.values()) {
            map.put(status.value, status);
        }
    }
    public static TasksStatus valueOf(int noteType) {
        return (TasksStatus) map.get(noteType);
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name().toLowerCase();
    }

}
