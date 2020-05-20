package ru.staffbots.tools.tasks;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.tools.values.DateValue;

import java.util.Date;
import java.util.function.Supplier;

/**
 *
 */
public class Task extends Thread {

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

    private TaskStatus status = TaskStatus.NEW;

    public TaskStatus getStatus() {
        return status;
    }

    private String statusString = null;

    public String getStatusString() {
        return statusString;
    }

    private String note;

    public String getNote() {
        return note;
    }

    private Supplier<Long> delaySupplier;

    public  Supplier<Long> getDelaySupplier() {
        return delaySupplier;
    }

    private Runnable action;

    public Runnable getAction() {
        return action;
    }

    private boolean silenceMode = false;

    public boolean getSilenceMode() {
        return silenceMode;
    }

    public Task(Task patternTask){
        this.note = patternTask.getNote();
        this.silenceMode = patternTask.getSilenceMode();
        this.delaySupplier = patternTask.getDelaySupplier();
        this.action = patternTask.getAction();
    }

    public Task(String note, Supplier<Long> delaySupplier, Runnable action){
        this.note = note;
        this.delaySupplier = delaySupplier;
        this.action = action;
    }

    public Task(String note, boolean silenceMode, Supplier<Long> delaySupplier, Runnable action){
        this.note = note;
        this.silenceMode = silenceMode;
        this.delaySupplier  = delaySupplier;
        this.action = action;
    }

    @Override
    public void run() {
        long delay = delaySupplier.get();
        if (delay < 0) {
            status = TaskStatus.OLD;
            statusString = null;
            return;
        }
        try {
            if (!silenceMode) {
                status = TaskStatus.WAITING;
                String runTime = DateValue.toString(new Date(System.currentTimeMillis() + delay), DateFormat.DATETIME);
                statusString = status.getDescription(Languages.getDefaultCode()) + " (" + runTime + ")";
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
            statusString = status.getDescription(Languages.getDefaultCode());
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
