package ru.staffbots.database.journal;

import ru.staffbots.tools.Translator;

import java.util.Date;

public class Note {

    private Date date;
    private String name;
    private String variables;
    private static final String variableSeparator = "\n";
    private NoteType type;

    public Note(Date date, NoteType type, String name, String... variables){
        this.name = name;
        this.variables = "";
        for (int i = 0; i < variables.length; i++)
            this.variables += ( i==0 ? "" : variableSeparator ) + variables[i];
        this.type = type;
        this.date = date;
    }

    @Override
    public String toString() {
        String note = Translator.getValue(type.getName(), name);
        if (variables != null) {
            String[] variables = getVariables().split(variableSeparator);
            for (int i = 0; i < variables.length; i++)
                note = note.replaceFirst("%s%", variables[i]);
        }
        //note = note.replaceAll("%s%","");
        return type.getDescription() + ": " + note;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public NoteType getType() {
        return type;
    }

    public String getMessage() {
        return toString();
    }

    public String getVariables() {
        return variables;
    }

}
