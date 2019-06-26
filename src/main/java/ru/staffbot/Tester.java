package ru.staffbot;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.tools.dates.Period;
import ru.staffbot.tools.Converter;
import ru.staffbot.tools.dates.DateFormat;
import ru.staffbot.tools.dates.DateScale;
import ru.staffbot.tools.botprocess.BotProcess;
import ru.staffbot.tools.botprocess.BotProcessStatus;
import ru.staffbot.tools.botprocess.BotTask;
import ru.staffbot.tools.devices.Device;
import ru.staffbot.tools.devices.Devices;
import ru.staffbot.tools.devices.hardware.ButtonDevice;
import ru.staffbot.tools.devices.hardware.RelayDevice;
import ru.staffbot.tools.devices.hardware.SensorDHT22Device;
import ru.staffbot.tools.devices.hardware.SonarHCSR04Device;
import ru.staffbot.tools.levers.*;
import ru.staffbot.tools.values.Value;

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
            "Светодиод", RaspiPin.GPIO_01, false);
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
