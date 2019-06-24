package ru.staffbot.utils.botprocess;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.settings.Settings;

import java.util.*;

public class BotProcess {

    private static long startTime = 0;

    private static BotProcessStatus status = BotProcessStatus.STOP;

    public static ArrayList<BotTask> list = new ArrayList();

    public static BotProcessStatus getStatus(){
        return status;
    }

    public static BotProcessStatus setStatus(BotProcessStatus status){
        switch (status){
            case START: start(); break;
            case PAUSE: pause(); break;
            case STOP: stop(); break;
            default: break;
        }
        reScheduleAll();
        return BotProcess.status;
    }

    public static void reScheduleAll(){
        for (int index = 0; index<list.size(); index++)
            reSchedule(list.get(index));
    }

    public static void reSchedule(BotTask task) {
        if (task.isNew()) {
            if (status == BotProcessStatus.START)
                task.start();
        } else if (task.isWaiting()) {
            task.interrupt();
        } else if (task.isExecution()) {
            if (status == BotProcessStatus.STOP)
                task.interrupt();
        } else if (task.isOld()) {
            int index = list.indexOf(task);
            list.remove(task);
            task = new BotTask(task.note, task.delay, task.action);
            list.add(index, task);
            if (status == BotProcessStatus.START)
                task.start();
        }
    }
    /**
     * Пуск всех заданий
     */
    private static void start(){
        if(status != BotProcessStatus.START){
            if (status != BotProcessStatus.PAUSE)
                setStartTime((new Date()).getTime());
            status = BotProcessStatus.START;
            Journal.add("Выполнен пуск", NoteType.WRINING);
        }
    }

    /**
     * Приостановка всех заданий
     */
    private static void pause(){
        if(status == BotProcessStatus.START) {
            status = BotProcessStatus.PAUSE;
            Journal.add("Произведена приостановка", NoteType.WRINING);
        }
    }

    /**
     * Остановка всех заданий
     */
    private static void stop(){
        if(status != BotProcessStatus.STOP) {
            status = BotProcessStatus.STOP;
            setStartTime(0);
            Journal.add("Произведена остановка", NoteType.WRINING);
        }
    }

    /*
     * Выполняем инициализацию перед первым пуском
     */
    public static void init(BotTask... tasks){
        list.clear();
        for (BotTask task:tasks)
            list.add(task);
        status = BotProcessStatus.START;
        setStatus((getStartTime() == 0) ? BotProcessStatus.STOP : BotProcessStatus.PAUSE);
        Journal.add("Задания проинициализированы");
        //Timer timer = new Timer();
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
