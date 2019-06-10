package ru.staffbot.utils.tasks;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.settings.Settings;

import java.util.*;

public class Tasks{



    private static long startTime = 0;

    private static TaskStatus status = TaskStatus.STOP;

    private static ArrayList<Task> list = new ArrayList();

    public static TaskStatus getStatus(){
        return status;
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

    public static void reSchedule(Task... tasks) {
        if (tasks.length == 0)
            tasks = list.toArray(new Task[list.size()]);
        //timer.purge();
        for (Task task : tasks)
            // Если задача уже выполнена, то
            if (task.isCompleted) {
                // Следующий запуск ещё не выполнен
                // Ставим задачу в рассписание
                task.cancel();
                task.timer.purge();
                task = new Task(task.note, task.delay, task.action);
                task.timer.schedule(task, task.getDelay());
            }
        //timer.purge();
    }
    /**
     * Пуск всех заданий
     */
    public static void start(){
        if(status != TaskStatus.START){
            if (status != TaskStatus.PAUSE)
                setStartTime((new Date()).getTime());
            status = TaskStatus.START;
            reSchedule();
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
    public static void init(Task... tasks){
        list.clear();
        for (Task task:tasks) list.add(task);
        setStatus((getStartTime() == 0) ? TaskStatus.STOP : TaskStatus.PAUSE);
        Journal.add("Задачи проинициализированы");
    }

    public static long getStartTime(){
        String startTimeString = (new Settings("control_start_time")).load();
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
            (new Settings("control_start_time")).delete();
        startTime = time;
        if (startTime != 0)
            (new Settings("control_start_time")).save(Long.toString(startTime));
    }



}
