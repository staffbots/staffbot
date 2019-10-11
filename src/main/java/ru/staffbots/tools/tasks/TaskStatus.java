package ru.staffbots.tools.tasks;

import java.util.HashMap;
import java.util.Map;

public enum TaskStatus {

    NEW(0, "Новый"),
    WAITING(1, "Ожидание"),
    EXECUTION(2, "Выполнение"),
    OLD(3, "Старый");

    private String description;
    private int value;
    private static Map map = new HashMap<>();

    TaskStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }


    public String getDescription(){
        return description;
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

}
