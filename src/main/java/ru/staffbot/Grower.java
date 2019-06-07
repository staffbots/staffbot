package ru.staffbot;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbot.database.settings.Settings;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.DateScale;
import ru.staffbot.utils.devices.hardware.SensorDHT22Device;
import ru.staffbot.utils.devices.hardware.SonarHCSR04Device;
import ru.staffbot.utils.levers.*;
import ru.staffbot.utils.tasks.Task;
import ru.staffbot.utils.tasks.Tasks;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.devices.hardware.RelayDevice;

import java.util.Date;

public class Grower extends Staffbot {

    public static void main(String[] args) {

        propertiesInit(); // Загружаем конфигурацию сборки
        databaseInit(); // Подключаемся к базе данных
        devicesInit(); // Инициализируем список устройств
        leversInit(); // Инициализируем список элементов управления
        tasksInit(); // Инициализируем список задач
        webserverInit(); // Запускаем вебсервер
        windowInit(); // Открываем окно
    }

    static {
        solutionName = new Object(){}.getClass().getEnclosingClass().getSimpleName(); //"Grower"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  Devices
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>инициализация устройств</b><br>
     * Заполняется список устройств {@code WebServer.devices}<br>
     */
    public static void devicesInit() {
        Devices.init(  sensor, sonar, sunRelay, funRelay);
    }

    public static SensorDHT22Device sensor = new SensorDHT22Device("sensor",
            "Датчик температуры и влажности",RaspiPin.GPIO_07);
    public static SonarHCSR04Device sonar = new SonarHCSR04Device("sonar",
            "Сонар определения уровня воды, м", RaspiPin.GPIO_03, RaspiPin.GPIO_02);
    public static RelayDevice sunRelay = new RelayDevice("sunRelay",
            "Реле включения/выключения света", false, RaspiPin.GPIO_00);
    public static RelayDevice funRelay = new RelayDevice("funRelay",
            "Реле включения вентилятора", true, RaspiPin.GPIO_01);


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  Levers
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>инициализация рычагов управления</b><br>
     * Заполняется список рычагов управления {@code WebServer.levers}<br>
     * Внимание! Порядок перечисления групп и рычагов повторяется в веб-интерфейсе
     */
    public static void leversInit() {
        Levers.initGroup("Освещение и вентиляция", sunriseLever, sunsetLever, funDelayLever);
        Levers.initGroup("Подготовка раствора", volumeLever, phLever, ecLever, soluteLever);
        Levers.initGroup("Орошение", dayRateLever, nightRateLever, durationLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    public static DateLever sunriseLever = new DateLever("sunriseLever",
            "Время включения света", "8:30", DateFormat.SHORTTIME);
    public static DateLever sunsetLever = new DateLever("sunsetLever",
            "Время выключения света", "16:45", DateFormat.SHORTTIME);
    public static LongLever funDelayLever = new LongLever("funDelayLever",
            "Инертность вентилятора, мин", 0, 20, 2 * 60);

    public static LabelLever volumeLever = new LabelLever("52л",
            "Объём раствора");
    public static DoubleLever phLever = new DoubleLever("phLever",
            "Водородный показатель (кислотность), pH", 0.0, 5.6, 10.0);
    public static DoubleLever ecLever = new DoubleLever("ecLever",
            "Удельная электролитическая проводимость, EC", 0.0, 8.1, 10.0);
    public static BooleanLever soluteLever = new BooleanLever("soluteLever",
            "Подготовка раствора", false);

    public static LongLever dayRateLever = new LongLever("dayRateLever",
            "Дневная периодичность, мин", 0, 60, 24 * 60);
    public static LongLever nightRateLever = new LongLever("nightRateLever",
            "Ночьная периодичность, мин", 0, 120, 24 * 60);
    public static LongLever durationLever = new LongLever("durationLever",
            "Продолжительность, мин", 0, 15, 24 * 60);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  Tasks
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>инициализация задач</b><br>
     * Заполняется список задач {@code tasks}<br>
     */
    public static void tasksInit() {
        Tasks.init(initTask, sunriseTask, sunsetTask, funriseTask, funsetTask, irrigationTask);
    }

    /**
     * Расчёт времени запуска
     */
    public static Runnable initTask = () -> {
        //Grower.funTask.init(new Date((new Date()).getTime() + 1700),0);
        //Grower.irrigationTask.init(new Date(), 2000);
        //Journal.add(" < < < Расчёт времени запуска");
    };

    /**
     * Включение света
     */
    public static Task sunriseTask = new Task("Включение света", () -> {
        // Получаем дату и время следующего включения
        Date momentOn = sunriseLever.getNearFutureTime();
        // Расчитываем количество милисекунд, оставшееся до момента запуска
        long delay = momentOn.getTime() - (new Date()).getTime();
        try {
            // Ложимся спать до момента запуска
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            return;
        }
        // Просыпаемся и выполняем операцию
        sunRelay.set(true);
    });

    /**
     * Выключение света
     */
    public static Task sunsetTask = new Task("Выключение света", () -> {
        // Получаем дату и время следующего включения
        Date momentOn = sunsetLever.getNearFutureTime();
        // Расчитываем количество милисекунд, оставшееся до момента запуска
        long delay = momentOn.getTime() - (new Date()).getTime();
        try {
            // Ложимся спать до момента запуска
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            return;
        }
        // Просыпаемся и выполняем операцию
        sunRelay.set(false);
    });

    /**
     * Включение вентилятора
     */
    public static Task funriseTask = new Task("Включение вентилятора", () -> {
        // Получаем дату и время следующего включения
        Date momentOn = Converter.longToDate(
                sunriseLever.getNearFutureTime().getTime() - funDelayLever.getValue() * 60 * 1000);
        // Расчитываем количество милисекунд, оставшееся до момента запуска
        long delay = momentOn.getTime() - (new Date()).getTime();
        try {
            // Ложимся спать до момента запуска
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            return;
        }
        // Просыпаемся и выполняем операцию
        funRelay.set(true);
    });

    /**
     * Выключение вентилятора
     */
    public static Task funsetTask = new Task("Выключение вентилятора", () -> {

    });

    /**
     * Включаем орошение:
     * - подготовка раствора
     * - продолжительность орошения задаётся параметром
     */
    public static Task irrigationTask = new Task("Орошение", () -> {
        try {
            Thread.sleep(1000);
            Journal.add(" ----- Орошение закончилось");
        } catch (InterruptedException e) {
            Tasks.stop();
        }
    });
}
