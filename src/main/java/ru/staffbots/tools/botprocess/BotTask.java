package ru.staffbots.tools.botprocess;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.Converter;
import ru.staffbots.tools.dates.DateFormat;

import java.util.Date;

/**
 *
 */
public class BotTask extends Thread implements DelayFunction {

    public boolean isNew(){
        return (status == BotTaskStatus.NEW);
    }

    public boolean isWaiting(){
        return (status == BotTaskStatus.WAITING);
    }

    public boolean isExecution(){
        return (status == BotTaskStatus.EXECUTION);
    }

    public boolean isOld(){
        return (status == BotTaskStatus.OLD);
    }

    public BotTaskStatus status = BotTaskStatus.NEW;

    public String statusString = null;

    public String getStatusString() {
        return statusString;
    }

    public String note;

    public DelayFunction delay;

    public long getDelay(){
        return delay.getDelay();
    }

    protected Runnable action;

    protected boolean silenceMode = false;

    public BotTask(String note, DelayFunction delay, Runnable action){
        this.note = note;
        this.delay = delay;
        this.action = action;
    }

    public BotTask(String note, boolean silenceMode, DelayFunction delay, Runnable action){
        this.silenceMode = silenceMode;
        this.note = note;
        this.delay = delay;
        this.action = action;
    }

    @Override
    public void run() {
        long delay = getDelay();
        if (delay < 0) {
            status = BotTaskStatus.OLD;
            statusString = null;
            return;
        }
        try {
            if (!silenceMode) {
                status = BotTaskStatus.WAITING;
                statusString = "Запуск ожидается "
                        + Converter.dateToString(new Date(System.currentTimeMillis() + delay), DateFormat.DATETIME);
                Journal.add(note + ": " + statusString);
            }
            // Ожидаем запуск
            Thread.sleep(delay);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            Journal.add(note + ": Ожидание прервано", NoteType.WRINING);
        }

        if (!isInterrupted()) {
            status = BotTaskStatus.EXECUTION;
            statusString = "Выполняется";
            // Выполняем задачу
            action.run();

            if (!silenceMode) {
                Journal.add(note + ": Задание выполнено");
            }
            //Journal.add(note + ": выключение");
            // Просим запланировать следующий запуск
        }
        status = BotTaskStatus.OLD;
        statusString = null;
        //if (!isInterrupted())
        BotProcess.reSchedule(this);
    }
}
