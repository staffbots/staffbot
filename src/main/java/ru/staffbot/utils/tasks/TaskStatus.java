package ru.staffbot.utils.tasks;

import java.util.HashMap;
import java.util.Map;

public enum TaskStatus {

    START(0, "Пуск"),
    PAUSE(1, "Пауза"),
    STOP(2, "Стоп");

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
        for (TaskStatus pageType : TaskStatus.values()) {
            map.put(pageType.value, pageType);
        }
    }
    public static TaskStatus valueOf(int noteType) {
        return (TaskStatus) map.get(noteType);
    }

    public int getValue() {
        return value;
    }

}
