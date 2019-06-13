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

    synchronized public boolean isNew(){
        return (status == BotTaskStatus.NEW);
    }
    synchronized public boolean isWaiting(){
        return (status == BotTaskStatus.WAITING);
    }

    synchronized public boolean isExecution(){
        return (status == BotTaskStatus.EXECUTION);
    }

    synchronized public boolean isOld(){
        return (status == BotTaskStatus.OLD);
    }

    public BotTaskStatus status = BotTaskStatus.NEW;

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

    //@Override
    public void run() {
        status = BotTaskStatus.WAITING;
        try {
            long delay = getDelay();
            Journal.add("# " + note + ": Запуск задания запланирован на "
                    + Converter.dateToString(new Date(System.currentTimeMillis() + delay), DateFormat.DATETIME));
            // Ожидаем запуск
            Thread.sleep(delay);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            Journal.add("# " + note + ": Ожидание прервано", NoteType.WRINING);
        }

        if (!isInterrupted()) {
            status = BotTaskStatus.EXECUTION;
            // Выполняем задачу
            action.run();
            // Просим запланировать следующий запуск
        }
        status = BotTaskStatus.OLD;
        //if (!isInterrupted())
        BotProcess.reSchedule(this);
    }
}
