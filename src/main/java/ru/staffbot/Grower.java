package ru.staffbot;

import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.tools.Converter;
import ru.staffbot.tools.dates.Period;
import ru.staffbot.tools.dates.DateFormat;
import ru.staffbot.tools.dates.DateScale;
import ru.staffbot.tools.values.DateValue;
import ru.staffbot.tools.values.Value;
import ru.staffbot.tools.levers.*;
import ru.staffbot.tools.devices.Device;
import ru.staffbot.tools.devices.Devices;
import ru.staffbot.tools.devices.hardware.SensorDHT22Device;
import ru.staffbot.tools.devices.hardware.SonarHCSR04Device;
import ru.staffbot.tools.devices.hardware.RelayDevice;
import ru.staffbot.tools.botprocess.BotTask;
import ru.staffbot.tools.botprocess.BotProcess;
import ru.staffbot.tools.botprocess.BotProcessStatus;
import com.pi4j.io.gpio.RaspiPin;
import java.util.Date;

/*
 *
 */
public class Grower extends Staffbot {

    // Точка входа при запуске приложения
    // ВНИМАНИЕ! Порядок инициализаций менять не рекомендуется
    public static void main(String[] args) {
        propertiesInit(); // Загружаем конфигурацию сборки
        databaseInit(); // Подключаемся к базе данных
        devicesInit(); // Инициализируем список устройств
        leversInit(); // Инициализируем список элементов управления
        botProcessInit(); // Инициализируем список заданий
        webserverInit(); // Запускаем веб-сервер
        windowInit(); // Открываем главное окно приложения
    }

    // Определяем наименование решения по названию текущего класса
    // solutionName определён в родительском классе Staffbot
    static {
        solutionName = new Object(){}.getClass().getEnclosingClass().getSimpleName(); //"Grower"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Levers - Рычаги
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация рычагов управления</b><br>
     * Заполняется список рычагов управления {@code WebServer.levers}<br>
     * ВНИМАНИЕ! Порядок перечисления групп и рычагов повторяется в веб-интерфейсе
     */
    private static void leversInit() {
        Levers.initGroup("Освещение и вентиляция", sunriseLever, sunsetLever, funUsedLever, funDelayLever);
        Levers.initGroup("Подготовка раствора", phLever, ecLever, soluteLever, volumeLever);
        Levers.initGroup("Орошение", dayRateLever, nightRateLever, durationLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    private static DateLever sunriseLever = new DateLever("sunriseLever",
            "Время включения света", DateFormat.SHORTTIME, "8:30");
    private static DateLever sunsetLever = new DateLever("sunsetLever",
            "Время выключения света", DateFormat.SHORTTIME, "16:45");
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
    private static DoubleLever volumeLever = new DoubleLever("volumeLever",
            "Объём раствора, л", LeverMode.OBSERVABLE);

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
     * Заполняется список устройств<br>
     */
    private static void devicesInit() {
        Devices.init(sensor, sonar, sunRelay, funRelay);
    }

    private static SensorDHT22Device sensor = new SensorDHT22Device("sensor",
            "Датчик температуры и влажности",RaspiPin.GPIO_07);
    private static SonarHCSR04Device sonar = new SonarHCSR04Device("sonar",
            "Сонар определения уровня воды, м", RaspiPin.GPIO_03, RaspiPin.GPIO_02);
    private static RelayDevice sunRelay = new RelayDevice("sunRelay",
            "Реле питания освещения", RaspiPin.GPIO_00, false);
    private static RelayDevice funRelay = new RelayDevice("funRelay",
            "Реле питания вентиляции", RaspiPin.GPIO_01, false);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Tasks - Зададия
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация заданий</b><br>
     * Заполняется список заданий <br>
     */
    private static void botProcessInit() {

        BotProcess.init(testTask, lightTask, ventingTask, irrigationTask);
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
                //double level = sonar.getDistance();
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

    /*****************************************************
     * Заполнение БД тестовыми случайными значениями                              *
     *****************************************************/
    private static String testTaskNote = "Заполнение БД";
    private static BotTask testTask = new BotTask(
            testTaskNote,
            () -> { // Расчёт задержки перед следующим запуском задания
                long delay = 0;
                return delay;
            },
            () -> { // Задание без повторений
                long timePeriod = DateScale.WEEK.getMilliseconds();
                Period period = new Period(DateFormat.DATE, new Date(System.currentTimeMillis() - timePeriod), new Date());
                for (Device device : Devices.list)
                    for (Value value : device.getValues())
                        value.setRandom(period);
                for (Lever lever : Levers.list)
                    lever.toValue().setRandom(period);
                BotProcess.setStatus(BotProcessStatus.STOP);
                Journal.add(testTaskNote + ": Задание выполнено");
            }
    );
}
