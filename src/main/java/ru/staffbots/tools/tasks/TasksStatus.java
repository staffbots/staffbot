package ru.staffbots.tools.tasks;

import ru.staffbots.tools.languages.Languages;

public enum TasksStatus {

    START,
    PAUSE,
    STOP;

    public String getDescription(String languageCode){
        return Languages.get(languageCode).getValue("tasksstatus", getName());
    }

    public String getName() {
        return name().toLowerCase();
    }

}
