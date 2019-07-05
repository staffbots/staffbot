package ru.staffbots.database.cleaner;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.dates.DateScale;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.values.DateValue;
import ru.staffbots.tools.values.Value;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Чистильщик БД,
 * выполняет чистку таблиц по указанным параметрам по требованию и по расписанию
 * Экземпляр описан как статическое поле в классе Database
 */
public class Cleaner {

    // Количество (записей или суток) оставляемых в журнале
    private long journalValue;
    // Еденица измерения количества (записей или суток) оставляемых в журнале
    // true - если измеряется записями, в sys_setings пишется как "record"
    // false - если измеряется сутками, в sys_setings пишется как "day"
    private boolean journalMeasureIsRecord;
    // Количество (записей или суток) оставляемых в таблицах значений
    private long tablesValue;
    // Еденица измерения количества (записей или суток) оставляемых в таблицах значений
    // true - если измеряется записями, в sys_setings пишется как "record"
    // false - если измеряется сутками, в sys_setings пишется как "day"
    private boolean tablesMeasureIsRecord;
    private boolean autoCleaning;
    private long autoValue;
    private DateScale autoMeasure;
    // Формат даты автозапуска
    public static final DateFormat DATE_FORMAT = DateFormat.DATETIME;
    // Дата автозапуска
    private Date autoStart = new Date();
    private boolean timerIsRuning = false;
    private Timer timer;

    public Cleaner() {
        update();
    }

    public void refresh(){
        if(!Database.connected())return ;
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
                + DateValue.toString(delay, DATE_FORMAT));
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
                    cleanByCount(lever.toValue().getTable(), tablesValue) :
                    cleanByDate(lever.toValue().getTable(), tablesValue);

        Journal.add("Очистка базы. Всего удалено записей: " + count);
    }

    private void loadSettings(){
        journalValue = Database.settings.loadAsLong("dbclean_journal_value",99);
        journalMeasureIsRecord = Database.settings.loadAsBollean("dbclean_journal_measure","record", true);
        tablesValue = Database.settings.loadAsLong("dbclean_tables_value",30);
        tablesMeasureIsRecord = Database.settings.loadAsBollean("dbclean_tables_measure", "record", false);
        autoCleaning = Database.settings.loadAsBollean("dbclean_auto_cleaning", "on", false);
        autoValue = Database.settings.loadAsLong("dbclean_auto_value",1);
        try {
            autoMeasure = DateScale.valueOf(Database.settings.load("dbclean_auto_measure").toUpperCase());
        } catch (Exception exception) {
            autoMeasure = DateScale.DAY;
        }
        autoStart = DateValue.fromString(Database.settings.load("dbclean_auto_start"), DATE_FORMAT, autoStart);
    }

    private void saveSettings(){
        Database.settings.save("dbclean_journal_value", Long.toString(journalValue));
        Database.settings.save("dbclean_journal_measure", journalMeasureIsRecord ? "record" : "day");
        Database.settings.save("dbclean_tables_value", Long.toString(tablesValue));
        Database.settings.save("dbclean_tables_measure", tablesMeasureIsRecord ? "record" : "day");
        Database.settings.save("dbclean_auto_cleaning", autoCleaning ? "on" : "off");
        Database.settings.save("dbclean_auto_value", Long.toString(autoValue));
        Database.settings.save("dbclean_auto_measure", autoMeasure.toString().toLowerCase());
        Database.settings.save("dbclean_auto_start", DateValue.toString(autoStart, DATE_FORMAT));
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
