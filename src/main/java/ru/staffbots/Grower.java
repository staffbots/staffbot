package ru.staffbots;

import com.pi4j.system.SystemInfo;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.dates.DateAccuracy;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.values.DateValue;
import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.levers.*;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.SensorDHT22Device;
import ru.staffbots.tools.devices.drivers.SonarHCSR04Device;
import ru.staffbots.tools.devices.drivers.RelayDevice;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.tools.tasks.TasksStatus;
import com.pi4j.io.gpio.RaspiPin;

import java.lang.invoke.MethodHandles;
import java.util.Date;

/*
 *
 */
public class Grower extends Staffbot {

    // Точка входа при запуске приложения
    public static void main(String[] args) {
        initiateSolution(()->{
                    Levers.addGroup("Освещение и вентиляция", sunriseLever, sunsetLever, funUsedLever, funDelayLever);
                    Levers.addGroup("Подготовка раствора", phLever, ecLever, soluteLever, volumeLever);
                    Levers.addGroup("Орошение", dayRateLever, nightRateLever, durationLever);
                    Devices.addDevices(sensor, sonar, sunRelay, funRelay); // Инициализируем список устройств
                    Tasks.init(testTask, lightTask, ventingTask, irrigationTask);
                }
        );
    }

    static {
        setBoardType(SystemInfo.BoardType.RaspberryPi_3B);
        setSolutionName(MethodHandles.lookup().lookupClass().getSimpleName()); // current class name
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Levers - Рычаги
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static DateLever sunriseLever = new DateLever("sunriseLever",
            "Время включения света", DateFormat.SHORTTIME, "8:30");
    static DateLever sunsetLever = new DateLever("sunsetLever",
            "Время выключения света", DateFormat.SHORTTIME, "16:45");
    static BooleanLever funUsedLever = new BooleanLever("fanUsedLever",
            "Включить вентиляцию", true);
    static LongLever funDelayLever = new LongLever("fanDelayLever",
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

    /*****************************************************
     * Освещение                                         *
     *****************************************************/
    static String lightTaskNote = "Освещение";
    static Task lightTask = new Task(
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
                Journal.addAnyNote(NoteType.WARNING, lightTaskNote + ": Задание прервано");
            }
            Journal.addAnyNote(lightTaskNote + ": выключение");
            //Выключаем
            sunRelay.set(false);
        });

    /*****************************************************
     * Вентиляция                                        *
     *****************************************************/
    static String ventingTaskNote = "Вентиляция";
    static Task ventingTask = new Task(
        ventingTaskNote,
        () -> { // Расчёт задержки перед следующим запуском
            if (!funUsedLever.getValue()) return new Long(-1);
            Date funriseDate = new Date(sunriseLever.getValue().getTime() - funDelayLever.getValue() * DateAccuracy.MINUTE.getMilliseconds());
            long funriseTime = DateValue.getNearFuture(funriseDate).getTime();
            Date funsetDate = new Date(sunsetLever.getValue().getTime() + funDelayLever.getValue() * DateAccuracy.MINUTE.getMilliseconds());
            long funsetTime = DateValue.getNearFuture(funsetDate).getTime();
            // Если ближайший закат наступает раньше чем рассвет, то
            long delay = (funsetTime < funriseTime) ? 0 : (funriseTime - System.currentTimeMillis());
            return delay;
        },
        () -> { // Задание
            try {
                Date funsetDate = new Date(sunsetLever.getValue().getTime() + funDelayLever.getValue() * DateAccuracy.MINUTE.getMilliseconds());
                long funsetTime = DateValue.getNearFuture(funsetDate).getTime();
                Journal.add(ventingTaskNote + ": включение до " +
                        DateValue.toString(new Date(funsetTime), DateFormat.DATETIME));
                //Включаем
                funRelay.set(true);
                Thread.sleep(funsetTime - System.currentTimeMillis());
            } catch (InterruptedException exception) {
                Journal.addAnyNote(NoteType.WARNING,ventingTaskNote + ": Задание прервано");
                //Thread.currentThread().interrupt();
            } finally {
                Journal.addAnyNote(ventingTaskNote + ": выключение");
                //Выключаем
                funRelay.set(false);
            }
        });

    /*****************************************************
     * Орошение                                          *
     *****************************************************/
    static String irrigationTaskNote = "Орошение";
    static Task irrigationTask = new Task(
        irrigationTaskNote,
        ()->{// Расчёт задержки перед следующим запуском
                return new Long(900000);
            },
        () -> {// Метод самой
            try {
                Journal.addAnyNote(irrigationTaskNote + ": Проверка уровня воды");
                //double level = sonar.getDistance();
                double volume = 3d / 5d;
                volumeLever.setFromString(String.format("%.3f", volume));
                volumeLever.setFromString(sunRelay.get() ? "День" : "Ночь" );
                //Вырввнивание уровня
                Thread.sleep(1000);
                Journal.addAnyNote(irrigationTaskNote + ": Проверка раствора на Ph и EC");
                //Замес Ph и EC
                Thread.sleep(5000);
                Journal.addAnyNote(irrigationTaskNote + ": Затопление");
                // Продолжительность затопления определена durationLever
                Thread.sleep(7000);
                Journal.addAnyNote(irrigationTaskNote + ": Слив");
            } catch (InterruptedException exception) {
                Journal.addAnyNote(NoteType.WARNING, irrigationTaskNote + ": Задание прервано");
            } finally {
                // Выключаем
                Journal.addAnyNote(irrigationTaskNote + ": выполнено");
            }
        });

    /*****************************************************
     * Заполнение БД тестовыми случайными значениями     *                            *
     *****************************************************/
    static String testTaskNote = "Заполнение БД";
    static Task testTask = new Task(
            testTaskNote,
            () -> { // Расчёт задержки перед следующим запуском задания
                long delay = 0;
                return delay;
            },
            () -> { // Задание без повторений
                long timePeriod = DateAccuracy.WEEK.getMilliseconds();
                Period period = new Period(DateFormat.DATE, new Date(System.currentTimeMillis() - timePeriod), new Date());
                for (Device device : Devices.getList())
                    for (Value value : device.getValues())
                        value.setRandom(period);
                for (Lever lever : Levers.getList())
                    lever.toValue().setRandom(period);
                Tasks.setStatus(TasksStatus.STOP);
                Journal.addAnyNote(testTaskNote + ": Задание выполнено");
            });

}
