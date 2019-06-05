package ru.staffbot.database.settings;

import ru.staffbot.database.DBTable;

public class Settings extends DBTable {

    public static final String DB_TABLE_NAME = "sys_settings";
    public static final String DB_TABLE_FIELDS = "configname VARCHAR(50), configvalue TEXT";

    public Settings(){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
    }

}
