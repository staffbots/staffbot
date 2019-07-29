package ru.staffbots;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.dates.DateScale;
import ru.staffbots.tools.values.DateValue;
import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.levers.*;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.SensorDHT22Device;
import ru.staffbots.tools.devices.drivers.SonarHCSR04Device;
import ru.staffbots.tools.devices.drivers.RelayDevice;
import ru.staffbots.tools.botprocess.BotTask;
import ru.staffbots.tools.botprocess.BotProcess;
import ru.staffbots.tools.botprocess.BotProcessStatus;
import com.pi4j.io.gpio.RaspiPin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

/*
 *
 */
public class Grower extends Pattern {

    // Точка входа при запуске приложения
    // ВНИМАНИЕ! Порядок инициализаций менять не рекомендуется
    public static void main(String[] args) {
        propertiesInit(); // Загружаем свойства из cfg-файла
        databaseInit(); // Подключаемся к базе данных
        leversInit(); // Инициализируем список элементов управления
        devicesInit(); // Инициализируем список устройств
        botProcessInit(); // Инициализируем список заданий
        webserverInit(); // Запускаем веб-сервер
        windowInit(); // Открываем главное окно приложения
//        System.out.println(GasSensorFC22.);
//        try {
//            java.awt.Desktop.getDesktop().browse(sensor.getURL().toURI());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
    }

    // Определяем наименование решения по названию текущего класса
    // solutionName определён в родительском классе Pattern
    static {
        solutionName = new Object(){}.getClass().getEnclosingClass().getSimpleName();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Levers - Рычаги
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация рычагов управления</b><br>
     * Заполняется список рычагов управления {@code WebServer.levers}<br>
     * ВНИМАНИЕ! Порядок перечисления групп и рычагов повторяется в веб-интерфейсе
     */
    static void leversInit() {
        Levers.initGroup("Освещение и вентиляция", sunriseLever, sunsetLever, funUsedLever, funDelayLever);
        Levers.initGroup("Подготовка раствора", phLever, ecLever, soluteLever, volumeLever);
        Levers.initGroup("Орошение", dayRateLever, nightRateLever, durationLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    static DateLever sunriseLever = new DateLever("sunriseLever",
            "Время включения света", DateFormat.SHORTTIME, "8:30");
    static DateLever sunsetLever = new DateLever("sunsetLever",
            "Время выключения света", DateFormat.SHORTTIME, "16:45");
    static BooleanLever funUsedLever = new BooleanLever("funUsedLever",
            "Включить вентиляцию", true);
    static LongLever funDelayLever = new LongLever("funDelayLever",
            "Инертность вентилятора, мин", 0, 20, 2 * 60);

    static DoubleLever phLever = new DoubleLever("phLever",
            "Водородный показатель (кислотность), pH", 5, 0.0, 5.6, 10.0);
    static DoubleLever ecLever = new DoubleLever("ecLever",
            "Удельная электролитическая проводимость, EC", 1, 0.0, 8.1, 10.0);
    static BooleanLever soluteLever = new BooleanLever("soluteLever",
            "Подготовка раствора", false);
    static DoubleLever volumeLever = new DoubleLever("volumeLever",
            "Объём раствора, л", LeverMode.OBSERVABLE, 2);

    static LongLever dayRateLever = new LongLever("dayRateLever",
            "Дневная периодичность, мин", 0, 60, 24 * 60);
    static LongLever nightRateLever = new LongLever("nightRateLever",
            "Ночная периодичность, мин", 0, 120, 24 * 60);
    static LongLever durationLever = new LongLever("durationLever",
            "Продолжительность, мин", 0, 15, 24 * 60);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Devices - Устройства
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация устройств</b><br>
     * Заполняется список устройств<br>
     */
    static void devicesInit() {
        Devices.init(sensor, sonar, sunRelay, funRelay);
    }

    static SensorDHT22Device sensor = new SensorDHT22Device("sensor",
            "Датчик температуры и влажности",RaspiPin.GPIO_07);
    static SonarHCSR04Device sonar = new SonarHCSR04Device("sonar",
            "Сонар определения уровня воды, м", RaspiPin.GPIO_03, RaspiPin.GPIO_02);
    static RelayDevice sunRelay = new RelayDevice("sunRelay",
            "Реле питания освещения", RaspiPin.GPIO_00, false);
    static RelayDevice funRelay = new RelayDevice("funRelay",
            "Реле питания вентиляции", RaspiPin.GPIO_01, false);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Tasks - Зададия
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация заданий</b><br>
     * Заполняется список заданий <br>
     */
    static void botProcessInit() {

        BotProcess.init(testTask, lightTask, ventingTask, irrigationTask);
    }

    /*****************************************************
     * Освещение                                         *
     *****************************************************/
    static String lightTaskNote = "Освещение";
    static BotTask lightTask = new BotTask(
        lightTaskNote,
        () -> { // Расчёт задержки перед следующим запуском задания
            long sunriseTime = sunriseLever.getNearFuture().getTime();
            long sunsetTime = sunsetLever.getNearFuture().getTime();
            // Если ближайший закат наступает раньше чем рассвет,
            // значит на дворе день и свет нужно включить без промедления delay = 0
            long delay = (sunsetTime < sunriseTime) ? 0 : (sunriseTime - System.currentTimeMillis());
            return delay;
        },
        () -> { // Задание
            try {
                // "От заката до рассвета"
                long sunsetTime = sunsetLever.getNearFuture().getTime();
                Journal.add(lightTaskNote + ": включение до " +
                        DateValue.toString(new Date(sunsetTime), DateFormat.DATETIME));
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
    static String ventingTaskNote = "Вентиляция";
    static BotTask ventingTask = new BotTask(
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
                        DateValue.toString(new Date(funsetTime), DateFormat.DATETIME));
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
    static String irrigationTaskNote = "Орошение";
    static BotTask irrigationTask = new BotTask(
        irrigationTaskNote,
        ()->{// Расчёт задержки перед следующим запуском
            return 900000;
            },
        () -> {// Метод самой
            try {
                Journal.add(irrigationTaskNote + ": Проверка уровня воды");
                //double level = sonar.getDistance();
                double volume = 3d / 5d;
                volumeLever.setFromString(String.format("%.3f", volume));
                volumeLever.setFromString(sunRelay.get() ? "День" : "Ночь" );
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
    static String testTaskNote = "Заполнение БД";
    static BotTask testTask = new BotTask(
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
