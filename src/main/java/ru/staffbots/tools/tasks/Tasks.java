package ru.staffbots.tools.tasks;

import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;

import java.util.ArrayList;
import java.util.Date;

public class Tasks {

    private static long startTime = 0;

    private static TasksStatus status = TasksStatus.STOP;

    public static ArrayList<Task> list = new ArrayList();

    public static TasksStatus getStatus(){
        return status;
    }

    public static TasksStatus setStatus(TasksStatus status){
        switch (status){
            case START: start(); break;
            case PAUSE: pause(); break;
            case STOP: stop(); break;
            default: break;
        }
        reScheduleAll();
        return Tasks.status;
    }

    public static void reScheduleAll(){
        for (int index = 0; index<list.size(); index++)
            reSchedule(list.get(index));
    }

    public static void reSchedule(Task task) {
        if (task.isNew()) {
            if (status == TasksStatus.START)
                task.start();
        } else if (task.isWaiting()) {
            task.interrupt();
        } else if (task.isExecution()) {
            if (status == TasksStatus.STOP)
                task.interrupt();
        } else if (task.isOld()) {
            int index = list.indexOf(task);
            list.remove(task);
            task = new Task(task.note, task.delay, task.action);
            list.add(index, task);
            if (status == TasksStatus.START)
                task.start();
        }
    }
    /**
     * Пуск всех заданий
     */
    private static void start(){
        if(status != TasksStatus.START){
            if (status != TasksStatus.PAUSE)
                setStartTime((new Date()).getTime());
            status = TasksStatus.START;
            Journal.add("Выполнен пуск заданий", NoteType.WRINING);
        }
    }

    /**
     * Приостановка всех заданий
     */
    private static void pause(){
        if(status == TasksStatus.START) {
            status = TasksStatus.PAUSE;
            Journal.add("Произведена приостановка заданий", NoteType.WRINING);
        }
    }

    /**
     * Остановка всех заданий
     */
    private static void stop(){
        if(status != TasksStatus.STOP) {
            status = TasksStatus.STOP;
            setStartTime(0);
            Journal.add("Произведена остановка заданий", NoteType.WRINING);
        }
    }

    /*
     * Выполняем инициализацию перед первым пуском
     */
    public static void init(Task... tasks){
        list.clear();
        for (Task task:tasks)
            if (!list.contains(task))
                list.add(task);
        status = TasksStatus.START;
        setStatus((getStartTime() == 0) ? TasksStatus.STOP : TasksStatus.PAUSE);
        Journal.add("Задания проинициализированы");
    }

    public static long getStartTime(){
        String startTimeString = (Database.settings == null) ? null :
                Database.settings.load("control_start_time");
        if (startTimeString == null) return startTime;
        try {
            startTime = Long.parseLong(startTimeString);
        } catch (Exception exception){
            //Ignore
        }
        return startTime;
    }

    private static void setStartTime(long time){
        if ((time == 0) && (startTime != 0))
            Database.settings.delete("control_start_time");
        startTime = time;
        if (startTime != 0)
            Database.settings.save("control_start_time", Long.toString(startTime));
    }

}
