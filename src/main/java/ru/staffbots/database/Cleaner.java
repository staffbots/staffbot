package ru.staffbots.database;

import ru.staffbots.database.tables.DBTable;
import ru.staffbots.database.tables.Variables;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.dates.DateAccuracy;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.tools.values.DateValue;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.sql.Timestamp;

/*
 * Чистильщик БД,
 * выполняет чистку таблиц по указанным параметрам по требованию и по расписанию
 * Экземпляр описан как статическое поле в классе Database
 */
public class Cleaner {

    private Cleaner() {}

    private static final Cleaner instance = new Cleaner();

    private static final DateFormat dateFormat = DateFormat.DATETIME;

    // Количество (записей или суток) оставляемых в журнале
    private static long journalValue;

    // Еденица измерения количества (записей или суток) оставляемых в журнале
    // true - если измеряется записями, в sys_setings пишется как "record"
    // false - если измеряется сутками, в sys_setings пишется как "day"
    private static boolean journalMeasureIsRecord;

    // Количество (записей или суток) оставляемых в таблицах значений
    private static long tablesValue;

    // Еденица измерения количества (записей или суток) оставляемых в таблицах значений
    // true - если измеряется записями, в sys_setings пишется как "record"
    // false - если измеряется сутками, в sys_setings пишется как "day"
    private static boolean tablesMeasureIsRecord;

    private static boolean autoCleaning;

    private static long autoValue;

    private static DateAccuracy autoMeasure;

    // Формат даты автозапуска
    // Дата автозапуска
    private static Date autoStart = new Date();

    private static boolean timerIsRuning = false;

    private static Timer timer;

    private static void loadVariables(){
        journalValue = Variables.loadAsLong("dbclean_journal_value",99);
        journalMeasureIsRecord = Variables.loadAsBollean("dbclean_journal_measure","record", true);
        tablesValue = Variables.loadAsLong("dbclean_tables_value",30);
        tablesMeasureIsRecord = Variables.loadAsBollean("dbclean_tables_measure", "record", false);
        autoCleaning = Variables.loadAsBollean("dbclean_auto_cleaning", "on", false);
        autoValue = Variables.loadAsLong("dbclean_auto_value",1);
        autoMeasure = DateAccuracy.fromString(Variables.load("dbclean_auto_measure"), DateAccuracy.DAY);
        autoStart = DateValue.fromString(Variables.load("dbclean_auto_start"), dateFormat, autoStart);
    }

    private static void saveVariables(){
        Variables.save("dbclean_journal_value", Long.toString(journalValue));
        Variables.save("dbclean_journal_measure", journalMeasureIsRecord ? "record" : "day");
        Variables.save("dbclean_tables_value", Long.toString(tablesValue));
        Variables.save("dbclean_tables_measure", tablesMeasureIsRecord ? "record" : "day");
        Variables.save("dbclean_auto_cleaning", autoCleaning ? "on" : "off");
        Variables.save("dbclean_auto_value", Long.toString(autoValue));
        Variables.save("dbclean_auto_measure", autoMeasure.toString().toLowerCase());
        Variables.save("dbclean_auto_start", DateValue.toString(autoStart, dateFormat));
    }

    private static long cleanByCount(DBTable table, long count){
        long recordsCount = table.getRows();
        if (recordsCount <= count) return 0;
        Executor executor = new Executor(null);
        return executor.execUpdate("DELETE FROM " + table.getTableName() +
                " ORDER BY moment ASC LIMIT " + (recordsCount - count));
    }

    private static long cleanByDate(DBTable table, long days){
        Executor executor = new Executor(null);
        long lastTime = System.currentTimeMillis() - days * DateAccuracy.DAY.getMilliseconds();
        return executor.execUpdate("DELETE FROM " + table.getTableName() + " WHERE moment < ?",
                new Timestamp(lastTime).toString());
    }

    public static Cleaner getTable() {
        return instance;
    }

    public static void reload(){
        loadVariables();
        saveVariables();
    }

    public static void restart(){
        reload();
        if (timerIsRuning) {
            timer.cancel();
            timer.purge();
        }
        if (autoCleaning) {
            long period = autoValue * autoMeasure.getMilliseconds();
            timer = new Timer(true);
            TimerTask cleanTask = new TimerTask() {
                @Override
                public void run() {
                    clean();
                }
            };
            long dt = System.currentTimeMillis() - autoStart.getTime();
            Date delay = (dt < 0) ? autoStart :
                    new Date(autoStart.getTime() + period * (long) Math.ceil((double) dt/period));
            timer.scheduleAtFixedRate(cleanTask, delay, period);
            timerIsRuning = true;
            Journal.add(NoteType.WARNING, "turnon_clean", Long.toString(autoValue), autoMeasure.getDescription(Languages.getDefaultCode()), DateValue.toString(delay, dateFormat));
        } else {
            timerIsRuning = false;
            Journal.add(NoteType.WARNING, "turnoff_clean");
        }
    }

    public static void clean(){
        reload();
        long valueRecords = 0;
        Map<String, DBTable> tableList = Database.getTableList(false);
        for (String tableName: tableList.keySet())
            valueRecords += (tablesMeasureIsRecord) ?
                    cleanByCount(tableList.get(tableName), tablesValue) :
                    cleanByDate(tableList.get(tableName), tablesValue);
        long journalNotes = (journalMeasureIsRecord) ?
                cleanByCount(Journal.getInstance(), journalValue - 1) :
                cleanByDate(Journal.getInstance(), journalValue);
        Journal.add(NoteType.WARNING, "clean_database", Long.toString(valueRecords), Long.toString(journalNotes));
    }

    public static String getFormat() {
        return dateFormat.getFormat();
    }

}
