package ru.staffbot;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.DateScale;
import ru.staffbot.utils.devices.hardware.SensorDHT22Device;
import ru.staffbot.utils.devices.hardware.SonarHCSR04Device;
import ru.staffbot.utils.levers.*;
import ru.staffbot.utils.tasks.Task;
import ru.staffbot.utils.tasks.TaskStatus;
import ru.staffbot.utils.tasks.Tasks;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.devices.hardware.RelayDevice;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Grower extends Staffbot {

    public static Date getNearFutureTime(String date){
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(Converter.stringToDate(date, DateFormat.SHORTTIME));
        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
        resultCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
        resultCalendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));
        resultCalendar.set(Calendar.MILLISECOND, currentCalendar.get(Calendar.MILLISECOND));
        if (resultCalendar.getTime().before(new Date())) resultCalendar.add(Calendar.DAY_OF_MONTH, 1);
        return resultCalendar.getTime();
    }

    public static void main(String[] args) {
        System.out.println(Converter.dateToString(getNearFutureTime("13:00"), DateFormat.FULLDATETIME));

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
    ////  Levers - Рычаги
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация рычагов управления</b><br>
     * Заполняется список рычагов управления {@code WebServer.levers}<br>
     * Внимание! Порядок перечисления групп и рычагов повторяется в веб-интерфейсе
     */
    private static void leversInit() {
        Levers.initGroup("Освещение и вентиляция", sunriseLever, sunsetLever, funDelayLever);
        Levers.initGroup("Подготовка раствора", volumeLever, phLever, ecLever, soluteLever);
        Levers.initGroup("Орошение", dayRateLever, nightRateLever, durationLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    private static DateLever sunriseLever = new DateLever("sunriseLever",
            "Время включения света", "8:30", DateFormat.SHORTTIME);
    private static DateLever sunsetLever = new DateLever("sunsetLever",
            "Время выключения света", "16:45", DateFormat.SHORTTIME);
    private static LongLever funDelayLever = new LongLever("funDelayLever",
            "Инертность вентилятора, мин", 0, 20, 2 * 60);

    private static LabelLever volumeLever = new LabelLever("52л",
            "Объём раствора");
    private static DoubleLever phLever = new DoubleLever("phLever",
            "Водородный показатель (кислотность), pH", 0.0, 5.6, 10.0);
    private static DoubleLever ecLever = new DoubleLever("ecLever",
            "Удельная электролитическая проводимость, EC", 0.0, 8.1, 10.0);
    private static BooleanLever soluteLever = new BooleanLever("soluteLever",
            "Подготовка раствора", false);

    private static LongLever dayRateLever = new LongLever("dayRateLever",
            "Дневная периодичность, мин", 0, 60, 24 * 60);
    private static LongLever nightRateLever = new LongLever("nightRateLever",
            "Ночьная периодичность, мин", 0, 120, 24 * 60);
    private static LongLever durationLever = new LongLever("durationLever",
            "Продолжительность, мин", 0, 15, 24 * 60);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  Devices - Устройства
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
            "Реле включения/выключения света", false, RaspiPin.GPIO_00);
    private static RelayDevice funRelay = new RelayDevice("funRelay",
            "Реле включения вентилятора", true, RaspiPin.GPIO_01);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  Tasks - Зададия
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация заданий</b><br>
     * Заполняется список задач {@code tasks}<br>
     */
    private static void tasksInit() {
        Tasks.init(lightTask, ventingTask, irrigationTask);
    }

    /**
     * Управление светом
     */
    private static String lightTaskNote = "Управление светом";
    private static Task lightTask = new Task(
            lightTaskNote,
            () -> {// Метод расчёта времени запуска
                    Date sunriseTime = sunriseLever.getNearFuture();
                    Date sunsetTime = sunsetLever.getNearFuture();
                    // Если ближайший закат наступает раньше чем рассвет, то
                    if (sunsetTime.before(sunriseTime))
                        // за окном-то день!
                        sunriseTime = new Date();
                    Journal.add("# " + lightTaskNote + ": Очередное включение запланировано на "
                            + Converter.dateToString(sunriseTime,DateFormat.DATETIME));
                    return sunriseTime;
            },
            () -> {// Метод самой
                try {
                    // "От заката до рассвета"
                    long dayLength = sunsetLever.getNearFuture().getTime() - System.currentTimeMillis();
                    long min = (long) Math.floor(dayLength / DateScale.MINUTE.getMilliseconds());
                    long sec = (long) Math.floor((dayLength % DateScale.MINUTE.getMilliseconds()) / DateScale.SECOND.getMilliseconds());
                    Journal.add("# " + lightTaskNote + ": включение на " + min + " мин. " + sec + " сек. (до " +
                     Converter.dateToString(sunsetLever.getNearFuture(),DateFormat.DATETIME) + ")");
                    // Включаем
                    sunRelay.set(true);
                    Thread.sleep(dayLength);
                    // Выключаем
                    sunRelay.set(false);
                    Journal.add("# " + lightTaskNote + ": выключение");
                } catch (Exception exception) {
                    Journal.add("# " + lightTaskNote + ": "+ exception.getMessage(),NoteType.ERROR);
                }
            });

    /**
     * Управление вентилятором
     */
    private static String ventingTaskNote = "Вентиляция";
    private static Task ventingTask = new Task(
            ventingTaskNote,
            () -> {// Метод расчёта времени запуска
                //Date actionDate = sunriseLever.getValue().getTime() - funDelayLever.getValue() * DateScale.MINUTE;
                return new Date(System.currentTimeMillis() + 800000);
            },
            () -> {// Метод самой
                try {
                    funRelay.set(true);
                    Journal.add(ventingTaskNote + ": Вентилятор включён");
                    // long dt = sunsetLever.getValue().getTime() - sunriseLever.getValue().getTime();
                    //dt = dt - 2 * funDelayLever.getValue() * DateScale.MINUTE;
                    long dt = 12000;
                    Thread.sleep(dt);
                    funRelay.set(false);
                    Journal.add(ventingTaskNote + ": Вентилятор выключен");
                } catch (Exception exception) {
                    Journal.add(ventingTaskNote + ": "+ exception.getMessage(),NoteType.ERROR);
                }
            });

    /**
     * Включаем орошение:
     * - подготовка раствора
     * - продолжительность орошения задаётся параметром
     */
    private static String irrigationTaskNote = "Орошение";
    private static Task irrigationTask = new Task(
        irrigationTaskNote,
        ()->{// Метод расчёта времени запуска
            return new Date(System.currentTimeMillis() + 500000);
            },
        () -> {// Метод самой
            try {
                Journal.add(irrigationTaskNote + ": Проверка уровня воды");
                double level = sonar.get();
                //Вырввнивание уровня
                Thread.sleep(1000);
                Journal.add(irrigationTaskNote + ": Проверка раствора на Ph и EC");
                //Замес Ph и EC
                Thread.sleep(1000);
                Journal.add(irrigationTaskNote + ": Затопление");
                // Продолжительность затопления определена durationLever
                Thread.sleep(2000);
                Journal.add(irrigationTaskNote + ": Слив");
                Journal.add(irrigationTaskNote + ": Орошение закончилось");
//                if(Tasks.getStatus() == TaskStatus.PAUSE);
            } catch (Exception exception) {
                Journal.add(irrigationTaskNote + ": " + exception.getMessage(), NoteType.ERROR);
            }
        });
}
