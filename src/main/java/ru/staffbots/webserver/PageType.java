package ru.staffbots.webserver;

import ru.staffbots.tools.languages.Languages;

public enum PageType {

    ENTRY   (-1, false),
    BASE    (-1, false),
    CONTROL ( 1, true ),
    STATUS  ( 0, true ),
    JOURNAL ( 0, true ),
    USERS   ( 2, true ),
    SYSTEM  ( 2, true ),
    ABOUT   ( 0, false);

    private int accessLevel;
    private boolean databaseDepend;

    PageType(int accessLevel, boolean databaseDepend) {
        this.accessLevel = accessLevel;
        this.databaseDepend = databaseDepend;
    }

    public String getDescription(String languageCode){
        return Languages.get(languageCode).getValue(getName(), "page_hint");
    }

    public String getCaption(String languageCode){
        return Languages.get(languageCode).getValue(getName(), "page_title");
    }

    public boolean getDatabaseDepend(){
        return databaseDepend;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public int getAccessLevel(){
        return accessLevel;
    }

}
