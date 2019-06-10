package ru.staffbot.utils.tasks;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class Task extends TimerTask implements DelayFunction{

    public Timer timer = new Timer();

    public boolean isCompleted;

    public String note;

    public DelayFunction delay = null;

    public Date getDelay(){
        return delay.getDelay();
    }

    protected Runnable action;

    public Task(String note, DelayFunction delay, Runnable action){
        this.note = note;
        this.delay = delay;
        this.action = action;
        isCompleted = true;
    }

    @Override
    public void run() {
        isCompleted = false;
        // Выполняем задачу
        action.run();
        // Отмечаем, что задача выполнена
        isCompleted = true;
        // Просим запланировать следующий запуск
        Tasks.reSchedule(this);
        //customHandler.postDelayed(this, 1000);
    }
}
