package ru.staffbot.utils.tasks;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.settings.Settings;

import java.util.ArrayList;
import java.util.Date;

public class Tasks{

    private static long startTime = 0;

    private static TaskStatus status = TaskStatus.STOP;

    private static ArrayList<Task> list = new ArrayList<Task>(0);

    public static TaskStatus getStatus(){
        return Tasks.status;
    }

    public static TaskStatus setStatus(TaskStatus status){
        switch (status){
            case START: start(); break;
            case PAUSE: pause(); break;
            case STOP: stop(); break;
            default: break;
        }
        return Tasks.status;
    }

    /**
     * Управляющий поток (расчёт даты и периода запуска)
     */
    private static Runnable tasksInit;

    /**
     * Пуск всех заданий
     */
    public static void start(){
        if(status != TaskStatus.START){
            if (status != TaskStatus.PAUSE)
                setStartTime((new Date()).getTime());
            status = TaskStatus.START;
            Journal.add("Выполнен пуск", NoteType.WRINING);
        }
    }

    /**
     * Приостановка всех заданий
     */
    public static void pause(){
        if(status == TaskStatus.START) {
           status = TaskStatus.PAUSE;
           Journal.add("Произведена приостановка", NoteType.WRINING);
        }
    }

    /**
     * Остановка всех заданий
     */
    public static void stop(){
        if(status != TaskStatus.STOP) {
           status = TaskStatus.STOP;
           setStartTime(0);
           Journal.add("Произведена остановка", NoteType.WRINING);
        }
    }

    /*
     * Выполняем инициализацию перед первым пуском
     */
    public static void init(Runnable tasksInit, Task... tasks){
        status = (getStartTime() == 0) ? TaskStatus.STOP : TaskStatus.PAUSE;
        Tasks.tasksInit = tasksInit;
        list.clear();
        for (Task task:tasks)
            list.add(task);
        Journal.add("Задачи проинициализированы");
    }

    public static long getStartTime(){
        String startTimeString = (new Settings("control_start_time")).load();
        if (startTimeString == null) return startTime;
        try {
            startTime = Long.parseLong(startTimeString);
        } catch (Exception exception){
        }
        return startTime;
    }

    private static void setStartTime(long time){
        if ((time == 0) && (startTime != 0))
            (new Settings("control_start_time")).delete();
        startTime = time;
        if (startTime != 0)
            (new Settings("control_start_time")).save(Long.toString(startTime));
    }

}
