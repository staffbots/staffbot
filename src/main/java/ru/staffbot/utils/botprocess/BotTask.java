package ru.staffbot.utils.botprocess;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;

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

    public BotTask(String note, DelayFunction delay, Runnable action){
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
            status = BotTaskStatus.WAITING;
            statusString = "Запуск ожидается "
                    + Converter.dateToString(new Date(System.currentTimeMillis() + delay), DateFormat.DATETIME);
            Journal.add(note + ": " + statusString);
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
            // Просим запланировать следующий запуск
        }
        status = BotTaskStatus.OLD;
        statusString = null;
        //if (!isInterrupted())
        BotProcess.reSchedule(this);
    }
}
