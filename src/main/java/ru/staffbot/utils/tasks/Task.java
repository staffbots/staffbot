package ru.staffbot.utils.tasks;

import ru.staffbot.webserver.WebServer;
import ru.staffbot.database.journal.Journal;

import java.util.Date;

/**
 *
 */
public class Task extends Thread {

    /**
     * <b>Описание</b>,
     * используется при формировании веб-интерфейса в {@link WebServer}.
     */
    protected final String note;

    /**
     * <b>Дата и время запуска</b>
     */
    private Date date = null;

    /**
     * <b>Период запуска</b>
     */
    private long rate = 0;

    /**
     * <b>Действие</b>
     */
    protected Runnable action;

    public Task(String note, Runnable action){
        this.action = action;
        this.note = note;
    }

    private long getDelay(){
        return this.date.getTime() - (new Date()).getTime();
    }

    @Override
    public void run() {

        try {
            while (true) {
                if (this.date == null)
                    break;
                while (getDelay() <= 0) { // пока дата запуска в прошлом
                    if (rate <= 0)
                        return;
                    this.date = new Date(this.date.getTime() + rate);
                }
                sleep(getDelay());
                Journal.add(note + " по расписанию запуска задач");
                this.action.run();
                sleep(0); // помогает отлавливать InterruptedException из action
            }
        } catch (InterruptedException e) {
            return;
        }
    }


    /**
     * <b>Получить описание</b><br>
     * @return описание
     */
    public String getNote(){
        return note;
    }

    synchronized public void init(Date date, long rate){
        this.date = date;
        this.rate = rate;
    }

}
