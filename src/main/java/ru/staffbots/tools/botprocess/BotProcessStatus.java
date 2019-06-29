package ru.staffbots.tools.botprocess;

import java.util.HashMap;
import java.util.Map;

public enum BotProcessStatus {

    START(0, "Пуск"),
    PAUSE(1, "Пауза"),
    STOP(2, "Стоп");

    private String description;
    private int value;
    private static Map map = new HashMap<>();

    BotProcessStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }


    public String getDescription(){
        return description;
    }

    static {
        for (BotProcessStatus status : BotProcessStatus.values()) {
            map.put(status.value, status);
        }
    }
    public static BotProcessStatus valueOf(int noteType) {
        return (BotProcessStatus) map.get(noteType);
    }

    public int getValue() {
        return value;
    }

}
