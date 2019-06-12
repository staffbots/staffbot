package ru.staffbot.utils.botprocess;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.settings.Settings;

import java.util.*;

public class BotProcess {

    private static long startTime = 0;

    private static BotProcessStatus status = BotProcessStatus.STOP;

    private static ArrayList<BotTask> list = new ArrayList();

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
        return BotProcess.status;
    }

    public static void reSchedule(BotTask... tasks) {
        if (tasks.length == 0)
            tasks = list.toArray(new BotTask[list.size()]);

        // Чистим список, для последующего заполения теми же заданиями,
        // кроме уже выполненных и с добавлением планируемых

        for (BotTask task : tasks) {
            // Если
            if (status == BotProcessStatus.STOP) {
                if (task.isAlive())
                    task.interrupt();
                continue;
            }

            if (status == BotProcessStatus.PAUSE) {
                if (task.isWaiting())
                    task.interrupt();
                continue;
            }


            if (status == BotProcessStatus.START) {
                if (task.isWaiting()) { // Обновление параметров
                    task.interrupt();
                    task = new BotTask(task.note, task.delay, task.action);
                    task.start();
                    continue;
                }

                if (task.isOld()) {
                    task = new BotTask(task.note, task.delay, task.action);
                    task.start();
                    continue;
                }

                if (task.isNew())
                    task.start();
            }
        }
    }
    /**
     * Пуск всех заданий
     */
    public static void start(){
        if(status != BotProcessStatus.START){
            if (status != BotProcessStatus.PAUSE)
                setStartTime((new Date()).getTime());
            status = BotProcessStatus.START;
            reSchedule();
            Journal.add("Выполнен пуск", NoteType.WRINING);
        }
    }

    /**
     * Приостановка всех заданий
     */
    public static void pause(){
        if(status == BotProcessStatus.START) {
            status = BotProcessStatus.PAUSE;
            reSchedule();
            Journal.add("Произведена приостановка", NoteType.WRINING);
        }
    }

    /**
     * Остановка всех заданий
     */
    public static void stop(){
        if(status != BotProcessStatus.STOP) {
            status = BotProcessStatus.STOP;
            setStartTime(0);
            reSchedule();
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
        setStatus((getStartTime() == 0) ? BotProcessStatus.STOP : BotProcessStatus.PAUSE);
        Journal.add("Задачи проинициализированы");
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
