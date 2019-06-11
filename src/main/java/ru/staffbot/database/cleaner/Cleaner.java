package ru.staffbot.database.cleaner;

import ru.staffbot.database.DBTable;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.settings.Settings;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.DateScale;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.levers.Lever;
import ru.staffbot.utils.levers.Levers;
import ru.staffbot.utils.values.Value;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    public static final DateFormat DATE_FORMAT = DateFormat.DATETIME;
    //public DateFormat format = DateFormat.DATETIME;

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
            long period = autoValue * autoMeasure.getMilliseconds();
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
                + Converter.dateToString(delay, DATE_FORMAT));
        } else {
            timerIsRuning = false;
            Journal.add("Автоматическая очистка базы данных отменена");
        }
    }

    synchronized public void clean(){
        refresh();
        long count = 0;
        count += (journalMeasureIsRecord) ?
                cleanByCount(new Journal(), journalValue) :
                cleanByDate(new Journal(), journalValue);

        for (Device device : Devices.list)
            for (Value value : device.getValues())
                count += (tablesMeasureIsRecord) ?
                        cleanByCount(value, tablesValue) :
                        cleanByDate(value, tablesValue);

        for (Lever lever : Levers.list)
            count += (tablesMeasureIsRecord) ?
                    cleanByCount(lever.getTable(), tablesValue) :
                    cleanByDate(lever.getTable(), tablesValue);

        Journal.add("Очистка базы. Всего удалено записей: " + count);
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
        autoStart = Converter.stringToDate((new Settings("dbclean_auto_start")).load(), DATE_FORMAT , autoStart);
    }

    private void saveSettings(){
        (new Settings("dbclean_journal_value")).save(Long.toString(journalValue));
        (new Settings("dbclean_journal_measure")).save(journalMeasureIsRecord ? "record" : "day");
        (new Settings("dbclean_tables_value")).save(Long.toString(tablesValue));
        (new Settings("dbclean_tables_measure")).save(tablesMeasureIsRecord ? "record" : "day");
        (new Settings("dbclean_auto_cleaning")).save(autoCleaning ? "on" : "off");
        (new Settings("dbclean_auto_value")).save(Long.toString(autoValue));
        (new Settings("dbclean_auto_measure")).save(autoMeasure.toString().toLowerCase());
        (new Settings("dbclean_auto_start")).save(Converter.dateToString(autoStart, DATE_FORMAT));
    }

    private long cleanByCount(DBTable table, long count){
        long recordsCount = table.getRecordsCount();
        if (recordsCount <= count) return 0;
        String query = "DELETE FROM " + table.getTableName() +
                " ORDER BY moment ASC LIMIT " + (recordsCount - count);
        return table.deleteFromTable(query);
    }

    private long cleanByDate(DBTable table, long days){
        if (!Database.connected()) return 0;
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "DELETE FROM " + table.getTableName() + " WHERE moment < ?");
            long lastTime = System.currentTimeMillis() - days * DateScale.DAY.getMilliseconds();
            statement.setTimestamp(1, new Timestamp(lastTime));
            return table.deleteFromTable(statement);
        } catch (Exception exception) {
            return 0;
        }

    }

}
