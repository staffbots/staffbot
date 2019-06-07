package ru.staffbot.database.cleaner;

import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.settings.Settings;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.DateScale;
import ru.staffbot.utils.values.DateValue;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Cleaner {

    public long journalValue;
    public boolean journalMeasureIsRecord;
    public long tablesValue;
    public boolean tablesMeasureIsRecord;
    public boolean autoCleaning;
    public long autoValue;
    public DateScale autoMeasure;
    public Date autoStart = new Date();

    private Timer timer;
    private boolean timerIsRuning = false;

    public Cleaner() {
        update();
    }

    public void refresh(){
        loadSettings();
        saveSettings();
    }

    public void update(){
        refresh();
        if (timerIsRuning) {
            timer.cancel() ;
            timer.purge();
        }
        if (autoCleaning) {
            long period = autoValue * autoMeasure.getValue();
            timer = new Timer(true);
            TimerTask cleanTask = new TimerTask() {
                @Override
                public void run() {
                    Database.cleaner.clean();
                }
            };
            long dt = System.currentTimeMillis() - autoStart.getTime();
            Date delay = (dt < 0) ? autoStart :
                    new Date(autoStart.getTime() + period * (long) Math.ceil((double) dt/period));
            timer.scheduleAtFixedRate(cleanTask, delay, period);
            timerIsRuning = true;
            Journal.add("Настроена автоматическая очистка базы данных с периодом "
                + autoValue + " ("+ autoMeasure.getDescription() + "), первый запуск: "
                + Converter.dateToString(delay, DateFormat.DATETIME));
        } else {
            timerIsRuning = false;
            Journal.add("Автоматическая очистка базы данных отменена");
        }
    }

    synchronized public void clean(){
        refresh();
        Journal.add("Чистим базу " + Converter.dateToString(new Date(), DateFormat.TIME));
    }

    private void loadSettings(){
        journalValue = (new Settings("dbclean_journal_value")).loadAsLong(99);
        journalMeasureIsRecord = (new Settings("dbclean_journal_measure")).loadAsBollean("record", true);
        tablesValue = (new Settings("dbclean_tables_value")).loadAsLong(30);
        tablesMeasureIsRecord = (new Settings("dbclean_tables_measure")).loadAsBollean("record", false);
        autoCleaning = (new Settings("dbclean_auto_cleaning")).loadAsBollean("on", false);
        autoValue = (new Settings("dbclean_auto_value")).loadAsLong(1);
        try {
            autoMeasure = DateScale.valueOf((new Settings("dbclean_auto_measure")).load().toUpperCase());
        } catch (Exception exception) {
            autoMeasure = DateScale.DAY;
        }
        autoStart = Converter.stringToDate((new Settings("dbclean_auto_start")).load(), DateFormat.DATETIME, autoStart);
    }

    private void saveSettings(){
        (new Settings("dbclean_journal_value")).save(Long.toString(journalValue));
        (new Settings("dbclean_journal_measure")).save(journalMeasureIsRecord ? "record" : "day");
        (new Settings("dbclean_tables_value")).save(Long.toString(tablesValue));
        (new Settings("dbclean_tables_measure")).save(tablesMeasureIsRecord ? "record" : "day");
        (new Settings("dbclean_auto_cleaning")).save(autoCleaning ? "on" : "off");
        (new Settings("dbclean_auto_value")).save(Long.toString(autoValue));
        (new Settings("dbclean_auto_measure")).save(autoMeasure.toString().toLowerCase());
        (new Settings("dbclean_auto_start")).save(Converter.dateToString(autoStart, DateFormat.DATETIME));
    }

}
