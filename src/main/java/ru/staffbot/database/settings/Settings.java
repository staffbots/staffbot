package ru.staffbot.database.settings;

import ru.staffbot.database.DBTable;

public class Settings extends DBTable {

    public static final String DB_TABLE_NAME = "sys_settings";
    public static final String DB_TABLE_FIELDS = "settingname VARCHAR(50), settingvalue VARCHAR(100)";

    public Settings(){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
    }

}
