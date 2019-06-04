package ru.staffbot.utils.tasks;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;

import java.util.ArrayList;

public class Tasks{

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
        if(status != TaskStatus.START) {
            tasksInit.run(); //расчёт даты и периода запуска
            for (Task task : Tasks.list) {
//                if(status == TaskStatus.STOP)
//                    task.start();
//                if(status == TaskStatus.PAUSE)
//                    task.notify();
            }
            status = TaskStatus.START;
            Journal.add("Выполнен пуск", NoteType.WRINING);
        }
    }

    /**
     * Приостановка всех заданий
     */
    public static void pause(){
        if(status == TaskStatus.START) {
            for (Task task : Tasks.list) {
//                try {
//                    task.wait();
//                } catch (InterruptedException e) {
//                    Journal.add(e.getMessage(), NoteType.ERROR);
//                }
            }
            status = TaskStatus.PAUSE;
            Journal.add("Произведена приостановка", NoteType.WRINING);
        }
    }

    /**
     * Остановка всех заданий
     */
    public static void stop(){
        if(status != TaskStatus.STOP) {
            for (Task task : Tasks.list) {
                task.interrupt();
            }
            status = TaskStatus.STOP;
            Journal.add("Произведена остановка", NoteType.WRINING);
        }
    }

    /*
     * Выполняем инициализацию перед первым пуском
     */
    public static void init(Runnable tasksInit, Task... tasks){
        Tasks.tasksInit = tasksInit;
        list.clear();
        for (Task task:tasks)
            list.add(task);
        Journal.add("Задачи проинициализированы");
    }

}
