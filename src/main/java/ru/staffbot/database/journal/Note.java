package ru.staffbot.database.journal;

import ru.staffbot.database.journal.NoteType;

import java.util.Date;

public class Note {

    private Date date;
    private String value;
    private NoteType type;

    public Note(Date date, String value, NoteType type){
        this.value = value;
        this.type = type;
        this.date = date;
    }

    @Override
    public String toString() {
        return type.getDescription() + ": " + value;
    }

    public Date getDate() {
        return date;
    }

    public String getValue() {
        return value;
    }

    public NoteType getType() {
        return type;
    }

    public String getMessage() {
        return getType().getDescription() + ": " + getValue();
    }


}
