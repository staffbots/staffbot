package ru.staffbot;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.DateScale;
import ru.staffbot.utils.botprocess.BotProcess;
import ru.staffbot.utils.botprocess.BotTask;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.devices.hardware.RelayDevice;
import ru.staffbot.utils.levers.*;

import java.util.Date;

public class Tester extends Staffbot {

    public static void main(String[] args) {
        propertiesInit(); // Загружаем конфигурацию сборки
        databaseInit(); // Подключаемся к базе данных
        devicesInit(); // Инициализируем список устройств
        leversInit(); // Инициализируем список элементов управления
        botProcessInit(); // Инициализируем список задач
        webserverInit(); // Запускаем вебсервер
        windowInit(); // Открываем окно
    }

    static {
        solutionName = new Object(){}.getClass().getEnclosingClass().getSimpleName(); //"Grower"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Levers - Рычаги
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация рычагов управления</b><br>
     * Заполняется список рычагов управления {@code WebServer.levers}<br>
     * Внимание! Порядок перечисления групп и рычагов повторяется в веб-интерфейсе
     */
    private static void leversInit() {
        Levers.initGroup("Светодиод", ledOnLever, ledOffLever, ledUsedLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    private static DoubleLever ledOnLever = new DoubleLever("ledOnLever",
            "Продолжительность включения, сек", 0.1, 1.5, 10.0);
    private static DoubleLever ledOffLever = new DoubleLever("ledffLever",
            "Продолжительность выключения, сек", 0.1, 1.0, 10.0);
    private static BooleanLever ledUsedLever = new BooleanLever("ledUsedLever",
            "Работа", true);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Devices - Устройства
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация устройств</b><br>
     * Заполняется список устройств {@code WebServer.devices}<br>
     */
    private static void devicesInit() {
        Devices.init(  ledRelay);
    }

    private static RelayDevice ledRelay = new RelayDevice("ledRelay",
            "Светодиод", false, RaspiPin.GPIO_01);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Tasks - Зададия
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация заданий</b><br>
     * Заполняется список задач {@code tasks}<br>
     */
    private static void botProcessInit() {
        BotProcess.init(ledTask);
    }

    /*****************************************************
     * Освещение                                         *
     *****************************************************/
    private static String ledTaskNote = "Мигание светодиода";
    private static BotTask ledTask = new BotTask(
            ledTaskNote,
        () -> { // Расчёт задержки перед следующим запуском задания
            long delay = Math.round(ledOffLever.getValue() * DateScale.SECOND.getMilliseconds());
            return delay;
        },
        () -> { // Задание
            try {
                // "От заката до рассвета"
                long delay = Math.round(ledOnLever.getValue() * DateScale.SECOND.getMilliseconds());
                Journal.add(ledTaskNote + ": включение до " +
                        Converter.dateToString(new Date(System.currentTimeMillis() + delay), DateFormat.DATETIME));
                // Включаем
                ledRelay.set(true);
                Thread.sleep(delay);
            } catch (InterruptedException exception) {
                Journal.add(ledTaskNote + ": Задание прервано", NoteType.WRINING);
            }
            Journal.add(ledTaskNote + ": выключение");
            //Выключаем
            ledRelay.set(false);
        });

}
