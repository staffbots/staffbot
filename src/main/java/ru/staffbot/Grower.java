package ru.staffbot;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.DateScale;
import ru.staffbot.utils.botprocess.BotProcess;
import ru.staffbot.utils.botprocess.BotTask;
import ru.staffbot.utils.devices.hardware.SensorDHT22Device;
import ru.staffbot.utils.devices.hardware.SonarHCSR04Device;
import ru.staffbot.utils.levers.*;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.devices.hardware.RelayDevice;
import ru.staffbot.utils.values.DateValue;
import java.util.Date;

public class Grower extends Staffbot {

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
        Levers.initGroup("Освещение и вентиляция", sunriseLever, sunsetLever, funUsedLever, funDelayLever);
        Levers.initGroup("Подготовка раствора", phLever, ecLever, soluteLever, volumeLever);
        Levers.initGroup("Орошение", dayRateLever, nightRateLever, durationLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    private static DateLever sunriseLever = new DateLever("sunriseLever",
            "Время включения света", "8:30", DateFormat.SHORTTIME);
    private static DateLever sunsetLever = new DateLever("sunsetLever",
            "Время выключения света", "16:45", DateFormat.SHORTTIME);
    private static BooleanLever funUsedLever = new BooleanLever("funUsedLever",
            "Включить вентиляцию", true);
    private static LongLever funDelayLever = new LongLever("funDelayLever",
            "Инертность вентилятора, мин", 0, 20, 2 * 60);

    private static DoubleLever phLever = new DoubleLever("phLever",
            "Водородный показатель (кислотность), pH", 0.0, 5.6, 10.0);
    private static DoubleLever ecLever = new DoubleLever("ecLever",
            "Удельная электролитическая проводимость, EC", 0.0, 8.1, 10.0);
    private static BooleanLever soluteLever = new BooleanLever("soluteLever",
            "Подготовка раствора", false);
    private static LabelLever volumeLever = new LabelLever("",
            "Объём раствора, л");

    private static LongLever dayRateLever = new LongLever("dayRateLever",
            "Дневная периодичность, мин", 0, 60, 24 * 60);
    private static LongLever nightRateLever = new LongLever("nightRateLever",
            "Ночная периодичность, мин", 0, 120, 24 * 60);
    private static LongLever durationLever = new LongLever("durationLever",
            "Продолжительность, мин", 0, 15, 24 * 60);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Devices - Устройства
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация устройств</b><br>
     * Заполняется список устройств {@code WebServer.devices}<br>
     */
    private static void devicesInit() {
        Devices.init(  sensor, sonar, sunRelay, funRelay);
    }

    private static SensorDHT22Device sensor = new SensorDHT22Device("sensor",
            "Датчик температуры и влажности",RaspiPin.GPIO_07);
    private static SonarHCSR04Device sonar = new SonarHCSR04Device("sonar",
            "Сонар определения уровня воды, м", RaspiPin.GPIO_03, RaspiPin.GPIO_02);
    private static RelayDevice sunRelay = new RelayDevice("sunRelay",
            "Реле питания освещения", false, RaspiPin.GPIO_00);
    private static RelayDevice funRelay = new RelayDevice("funRelay",
            "Реле питания вентиляции", false, RaspiPin.GPIO_01);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Tasks - Зададия
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация заданий</b><br>
     * Заполняется список задач {@code tasks}<br>
     */
    private static void botProcessInit() {
        BotProcess.init(lightTask, ventingTask, irrigationTask);
    }

    /*****************************************************
     * Освещение                                         *
     *****************************************************/
    private static String lightTaskNote = "Освещение";
    private static BotTask lightTask = new BotTask(
        lightTaskNote,
        () -> { // Расчёт задержки перед следующим запуском задания
            long sunriseTime = sunriseLever.getNearFuture().getTime();
            long sunsetTime = sunsetLever.getNearFuture().getTime();
            // Если ближайший закат наступает раньше чем рассвет, то
            long delay = (sunsetTime < sunriseTime) ? 0 : (sunriseTime - System.currentTimeMillis());
            return delay;
        },
        () -> { // Задание
            try {
                // "От заката до рассвета"
                long sunsetTime = sunsetLever.getNearFuture().getTime();
                Journal.add(lightTaskNote + ": включение до " +
                        Converter.dateToString(new Date(sunsetTime), DateFormat.DATETIME));
                // Включаем
                sunRelay.set(true);
                Thread.sleep(sunsetTime - System.currentTimeMillis());
            } catch (InterruptedException exception) {
                Journal.add(lightTaskNote + ": Задание прервано", NoteType.WRINING);
            }
            Journal.add(lightTaskNote + ": выключение");
            //Выключаем
            sunRelay.set(false);
        });

    /*****************************************************
     * Вентиляция                                        *
     *****************************************************/
    private static String ventingTaskNote = "Вентиляция";
    private static BotTask ventingTask = new BotTask(
        ventingTaskNote,
        () -> { // Расчёт задержки перед следующим запуском
            if (!funUsedLever.getValue()) return -1;
            Date funriseDate = new Date(sunriseLever.getValue().getTime() - funDelayLever.getValue() * DateScale.MINUTE.getMilliseconds());
            long funriseTime = DateValue.getNearFuture(funriseDate).getTime();
            Date funsetDate = new Date(sunsetLever.getValue().getTime() + funDelayLever.getValue() * DateScale.MINUTE.getMilliseconds());
            long funsetTime = DateValue.getNearFuture(funsetDate).getTime();
            // Если ближайший закат наступает раньше чем рассвет, то
            long delay = (funsetTime < funriseTime) ? 0 : (funriseTime - System.currentTimeMillis());
            return delay;
        },
        () -> { // Задание
            try {
                Date funsetDate = new Date(sunsetLever.getValue().getTime() + funDelayLever.getValue() * DateScale.MINUTE.getMilliseconds());
                long funsetTime = DateValue.getNearFuture(funsetDate).getTime();
                Journal.add(ventingTaskNote + ": включение до " +
                        Converter.dateToString(new Date(funsetTime), DateFormat.DATETIME));
                //Включаем
                funRelay.set(true);
                Thread.sleep(funsetTime - System.currentTimeMillis());
            } catch (InterruptedException exception) {
                Journal.add(ventingTaskNote + ": Задание прервано", NoteType.WRINING);
                //Thread.currentThread().interrupt();
            } finally {
                Journal.add(ventingTaskNote + ": выключение");
                //Выключаем
                funRelay.set(false);
            }
        });

    /*****************************************************
     * Орошение                                          *
     *****************************************************/
    private static String irrigationTaskNote = "Орошение";
    private static BotTask irrigationTask = new BotTask(
        irrigationTaskNote,
        ()->{// Расчёт задержки перед следующим запуском
            return 900000;
            },
        () -> {// Метод самой
            try {
                Journal.add(irrigationTaskNote + ": Проверка уровня воды");
                double level = sonar.get();
                double volume = 3d / 5d;
                volumeLever.setValueFromString(String.format("%.3f", volume));
                volumeLever.setValueFromString(sunRelay.get() ? "День" : "Ночь" );
                //Вырввнивание уровня
                Thread.sleep(1000);
                Journal.add(irrigationTaskNote + ": Проверка раствора на Ph и EC");
                //Замес Ph и EC
                Thread.sleep(5000);
                Journal.add(irrigationTaskNote + ": Затопление");
                // Продолжительность затопления определена durationLever
                Thread.sleep(7000);
                Journal.add(irrigationTaskNote + ": Слив");
            } catch (InterruptedException exception) {
                Journal.add(irrigationTaskNote + ": Задание прервано",NoteType.WRINING);
            } finally {
                // Выключаем
                Journal.add(irrigationTaskNote + ": выполнено");
            }
        });
}
