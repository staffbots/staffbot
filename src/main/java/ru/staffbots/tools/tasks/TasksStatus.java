package ru.staffbots.tools.tasks;

import java.util.HashMap;
import java.util.Map;

public enum TasksStatus {

    START(0, "Пуск"),
    PAUSE(1, "Пауза"),
    STOP(2, "Стоп");

    private String description;
    private int value;
    private static Map map = new HashMap<>();

    TasksStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }


    public String getDescription(){
        return description;
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

}
