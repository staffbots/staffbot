package ru.staffbots.tools.tasks;

import ru.staffbots.tools.languages.Languages;

import java.util.HashMap;
import java.util.Map;

public enum TaskStatus {

    NEW(0),
    WAITING(1),
    EXECUTION(2),
    OLD(3);

    private int value;
    private static Map map = new HashMap<>();

    TaskStatus(int value) {
        this.value = value;
    }


    public String getDescription(String languageCode){
        return Languages.get(languageCode).getValue("taskstatus", getName());
    }

    static {
        for (TaskStatus status : TaskStatus.values()) {
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
