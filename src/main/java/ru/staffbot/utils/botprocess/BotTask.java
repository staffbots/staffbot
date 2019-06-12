package ru.staffbot.utils.botprocess;

import ru.staffbot.database.journal.Journal;
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
        status = BotTaskStatus.WAITING;
        try {
            long delay = getDelay();
            Journal.add("# " + note + ": Запуск задания запланирован на "
                    + Converter.dateToString(new Date(System.currentTimeMillis() + delay), DateFormat.DATETIME));
            // Ожидаем запуск
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            return;
        }

        status = BotTaskStatus.EXECUTION;
        // Выполняем задачу
        action.run();

        status = BotTaskStatus.OLD;
        // Просим запланировать следующий запуск
        BotProcess.reSchedule(this);
    }
}
