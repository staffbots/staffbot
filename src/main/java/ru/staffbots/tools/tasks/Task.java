package ru.staffbots.tools.tasks;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.values.DateValue;

import java.util.Date;

/**
 *
 */
public class Task extends Thread implements DelayFunction {

    public boolean isNew(){
        return (status == TaskStatus.NEW);
    }

    public boolean isWaiting(){
        return (status == TaskStatus.WAITING);
    }

    public boolean isExecution(){
        return (status == TaskStatus.EXECUTION);
    }

    public boolean isOld(){
        return (status == TaskStatus.OLD);
    }

    public TaskStatus status = TaskStatus.NEW;

    private String statusString = null;

    public String getStatusString() {
        return statusString;
    }

    public String note;

    public DelayFunction delay;

    public long getDelay(){
        return delay.getDelay();
    }

    protected Runnable action;

    private boolean silenceMode = false;

    public Task(String note, DelayFunction delay, Runnable action){
        this.note = note;
        this.delay = delay;
        this.action = action;
    }

    public Task(String note, boolean silenceMode, DelayFunction delay, Runnable action){
        this.silenceMode = silenceMode;
        this.note = note;
        this.delay = delay;
        this.action = action;
    }

    @Override
    public void run() {
        long delay = getDelay();
        if (delay < 0) {
            status = TaskStatus.OLD;
            statusString = null;
            return;
        }
        try {
            if (!silenceMode) {
                status = TaskStatus.WAITING;
                String runTime = DateValue.toString(new Date(System.currentTimeMillis() + delay), DateFormat.DATETIME);
                statusString = status.getDescription() + " (" + runTime + ")";
                Journal.add(note + ": " + statusString);
            }
            // Ожидаем запуск
            Thread.sleep(delay);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            Journal.add(NoteType.WARNING, "interrupte_task", note);
        }

        if (!isInterrupted()) {
            status = TaskStatus.EXECUTION;
            statusString = status.getDescription();
            // Выполняем задачу
            action.run();

            if (!silenceMode) {
                Journal.add("complite_task", note);
            }
            //Journal.add(note + ": выключение");
            // Просим запланировать следующий запуск
        }
        status = TaskStatus.OLD;
        statusString = null;
        //if (!isInterrupted())
        Tasks.reSchedule(this);
    }
}
