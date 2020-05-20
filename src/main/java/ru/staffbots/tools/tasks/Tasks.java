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
            task = new Task(task);
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
            Journal.add(NoteType.WARNING, "StartTasks");
        }
    }

    /**
     * Приостановка всех заданий
     */
    private static void pause(){
        if(status == TasksStatus.START) {
            status = TasksStatus.PAUSE;
            Journal.add(NoteType.WARNING, "PauseTasks");
        }
    }

    /**
     * Остановка всех заданий
     */
    private static void stop(){
        if(status != TasksStatus.STOP) {
            status = TasksStatus.STOP;
            setStartTime(0);
            Journal.add(NoteType.WARNING, "stop_tasks");
        }
    }

    /*
     * Инициализация
     */
    public static void init(Task... tasks){
        list.clear();
        if (tasks == null) tasks = new Task[0];
        for (Task task:tasks)
            if (!list.contains(task))
                list.add(task);
        status = TasksStatus.START;
        setStatus((getStartTime() == 0) ? TasksStatus.STOP : TasksStatus.PAUSE);
        if (tasks.length > 0)
            Journal.add("init_tasks");
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
