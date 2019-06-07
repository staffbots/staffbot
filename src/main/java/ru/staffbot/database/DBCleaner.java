package ru.staffbot.database;

import ru.staffbot.database.settings.Settings;

public class DBCleaner {

    public boolean autoCleaning;
    public long autoValue;
    //public long automeasure;


    public DBCleaner(){
        autoCleaning = new Settings("dbclean_auto_cleaning").loadAsBollean("on", false);
        autoValue = new Settings("dbclean_auto_value").loadAsLong(1);

        //automeasure = new Settings("dbclean_auto_measure").loadAsLong(1);
        //autoValue = new Settings("dbclean_auto_start").load();

    }

}
