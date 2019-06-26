package ru.staffbot.tools.botprocess;

import java.util.HashMap;
import java.util.Map;

public enum BotTaskStatus {

    NEW(0, "Новый"),
    WAITING(1, "Ожидание"),
    EXECUTION(2, "Выполнение"),
    OLD(3, "Старый");

    private String description;
    private int value;
    private static Map map = new HashMap<>();

    BotTaskStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }


    public String getDescription(){
        return description;
    }

    static {
        for (BotTaskStatus status : BotTaskStatus.values()) {
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
