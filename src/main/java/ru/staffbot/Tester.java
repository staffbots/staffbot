package ru.staffbot;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.journal.Period;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.DateScale;
import ru.staffbot.utils.botprocess.BotProcess;
import ru.staffbot.utils.botprocess.BotTask;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.devices.hardware.ButtonDevice;
import ru.staffbot.utils.devices.hardware.RelayDevice;
import ru.staffbot.utils.devices.hardware.SensorDHT22Device;
import ru.staffbot.utils.devices.hardware.SonarHCSR04Device;
import ru.staffbot.utils.levers.*;
import ru.staffbot.utils.values.DoubleValue;
import ru.staffbot.utils.values.Value;
import ru.staffbot.utils.values.ValueType;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Tester extends Staffbot {

    public static void main(String[] args) {
        System.out.println(TimeZone.getDefault().getRawOffset());
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
        Levers.initGroup("Светодиод горит пока не превышено:", distanceLever, usedLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    private static DoubleLever distanceLever = new DoubleLever("distanceLever",
            "Контрольное расстояние, см", 5.0, 20.0, 4000.0);
    private static BooleanLever usedLever = new BooleanLever("usedLever",
            "Работа", true);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Devices - Устройства
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация устройств</b><br>
     * Заполняется список устройств {@code WebServer.devices}<br>
     */
    private static void devicesInit() {
        Devices.init(  ledRelay, sensor, sonar, button);
    }

    private static RelayDevice ledRelay = new RelayDevice("ledRelay",
            "Светодиод", false, RaspiPin.GPIO_01);
    private static SensorDHT22Device sensor = new SensorDHT22Device("sensor",
            "Датчик температуры и влажности", RaspiPin.GPIO_25);
    private static SonarHCSR04Device sonar = new SonarHCSR04Device("sonar",
        "Сонар", RaspiPin.GPIO_04, RaspiPin.GPIO_05);
    private static ButtonDevice button = new ButtonDevice("button",
            "Кнопка", RaspiPin.GPIO_06, () -> {
        // Обработка нажатия кнопки
        //System.out.println(" Обработка нажатия кнопки");
        double distance = -1;
        if (!usedLever.getValue()) return;
        try {
            distance = sonar.getDistance();
            System.out.println("distance = " + distance + "cm");
            System.out.println("temperature = " + sensor.getTemperature() + "C");
            System.out.println("humidity = " + sensor.getHumidity() + "%");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            ledRelay.set(false);
        }
        ledRelay.set(distanceLever.getValue() < distance);
        //System.out.println("Lever = " + distanceLever.getValue());

    });
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Tasks - Зададия
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация заданий</b><br>
     * Заполняется список задач {@code tasks}<br>
     */
    private static void botProcessInit() {
//        BotProcess.init(ledTask);
        BotProcess.init(testTask);
    }

    /*****************************************************
     * Мигание светодиода                                *
     *****************************************************/
    private static String ledTaskNote = "Мигание светодиода";
    private static BotTask ledTask = new BotTask(
            ledTaskNote,
        () -> { // Расчёт задержки перед следующим запуском задания
            //long delay = Math.round(ledOffLever.getValue() * DateScale.SECOND.getMilliseconds());
            long delay = 0;
            return delay;
        },
        () -> { // Задание
            try {
                // "От заката до рассвета"
                //long delay = Math.round(ledOnLever.getValue() * DateScale.SECOND.getMilliseconds());
                long delay = 2000;
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

    /*****************************************************
     * Заполнение БД тестовыми случайными значениями                              *
     *****************************************************/
    private static String testTaskNote = "Заполнение БД";
    private static BotTask testTask = new BotTask(
            testTaskNote,
            () -> { // Расчёт задержки перед следующим запуском задания
                //long delay = Math.round(ledOffLever.getValue() * DateScale.SECOND.getMilliseconds());
                long delay = 0;
                return delay;
            },
            () -> { // Задание
                try {
                    // "От заката до рассвета"
                    //long delay = Math.round(ledOnLever.getValue() * DateScale.SECOND.getMilliseconds());
                    long timePeriod = DateScale.WEEK.getMilliseconds();
                    long count = 80;
                    Period period = new Period(DateFormat.DATE, new Date(System.currentTimeMillis() - timePeriod), new Date());
                    long moment = period.fromDate.getTime();
                    long newValue = 0;
                    for (Device device : Devices.list)
                        for (Value value : device.getValues())
                            if (value.dbStorage) {
                                value.eraseTable();
                                moment = period.fromDate.getTime();
                                double dis = Math.random() * 10;
                                double mat = Math.random() * 20;
                                while (moment < period.toDate.getTime()) {
                                    moment += timePeriod / count;
                                    newValue = 0;
                                    if (value.getValueType() == ValueType.DOUBLE)
                                        newValue = new DoubleValue("", "", Math.random() * mat - dis).get();
                                    if (value.getValueType() == ValueType.BOOLEAN)
                                        newValue = Math.round(Math.random());
                                    if (value.getValueType() == ValueType.LONG)
                                        newValue = Math.round(Math.random()  * mat - dis);
                                    value.set(new Date(moment), newValue);
                                }
                            }
                    long delay = 2000000;
                    Thread.sleep(delay);
                } catch (InterruptedException exception) {
                    Journal.add(ledTaskNote + ": Задание прервано", NoteType.WRINING);
                }
                Journal.add(ledTaskNote + ": выключение");
            });

}