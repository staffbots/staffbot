package ru.staffbots.database.tables.journal;

import ru.staffbots.tools.languages.Languages;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum NoteType {

    INFORMATION(0),
    WARNING(1),
    ERROR(2);

    private int value;

    private static Map map = new HashMap<>();

    NoteType(int value) {
        this.value = value;
    }

    public String getDescription(String languageCode){
        return Languages.get(languageCode).getValue("notetype", getName());
    }

    static {
        for (NoteType pageType : NoteType.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static NoteType valueOf(int noteType) {
        return (NoteType) map.get(noteType);
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name().toLowerCase();
    }

}
