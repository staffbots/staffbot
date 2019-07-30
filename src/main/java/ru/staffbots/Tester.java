package ru.staffbots;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.dates.DateScale;
import ru.staffbots.tools.botprocess.BotProcess;
import ru.staffbots.tools.botprocess.BotProcessStatus;
import ru.staffbots.tools.botprocess.BotTask;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.*;
import ru.staffbots.tools.levers.*;
import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.values.ValueMode;

import java.util.Date;
import java.util.TimeZone;

public class Tester extends Pattern {

    // Точка входа при запуске приложения
    // ВНИМАНИЕ! Порядок инициализаций менять не рекомендуется
    public static void main(String[] args) {
        propertiesInit(); // Загружаем конфигурацию сборки
        databaseInit(); // Подключаемся к базе данных
        leversInit(); // Инициализируем список элементов управления
        devicesInit(); // Инициализируем список устройств
        botProcessInit(); // Инициализируем список задач
        webserverInit(); // Запускаем вебсервер
        windowInit(); // Открываем окно
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
     * Внимание! Порядок перечисления групп и рычагов повторяется в веб-интерфейсе
     */
    static void leversInit() {
        Levers.initGroup(null, delayLever, buttonLever, listLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    static LongLever delayLever = new LongLever("delayLever",
            "Частота опроса, сек", ValueMode.TEMPORARY, 20, 2, 60*60);

    static ButtonLever buttonLever = new ButtonLever("buttonLever",
        "Выполнить","Калибровка датчика, основанная на триангуляции континума", () -> {
        // Обработка нажатия кнопки
        Journal.add("Нажата кнопка калибровки датчика");
    });

    static ListLever listLever = new ListLever("listLever",
            "Тестовый список", "строка 0","строка 1","строка 2","строка 3");

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Devices - Устройства
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация устройств</b><br>
     * Заполняется список устройств {@code WebServer.devices}<br>
     */
    static void devicesInit() {
        Devices.init(  ledDevice, sensor, sonar, button);
    }

    static LedDevice ledDevice = new LedDevice("led",
            "Индикатор конца света", RaspiPin.GPIO_01, false);
    static SensorDHT22Device sensor = new SensorDHT22Device("sensor",
            "Датчик температуры и влажности", RaspiPin.GPIO_25);
    static SonarHCSR04Device sonar = new SonarHCSR04Device("sonar",
        "Расстояние до врага", RaspiPin.GPIO_04, RaspiPin.GPIO_05);
    static ButtonDevice button = new ButtonDevice("button",
            "Ракетно ядерный залп", ValueMode.TEMPORARY, RaspiPin.GPIO_06, () -> {
        // Обработка нажатия кнопки
        double distance = -1;
        try {
//            distance = sonar.getDistance();
//            System.out.println("distance = " + distance + "cm");
//            System.out.println("temperature = " + sensor.getTemperature() + "C");
//            System.out.println("humidity = " + sensor.getHumidity() + "%");
        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            ledRelay.set(false);
        }
        //ledRelay.set(distanceLever.getValue() < distance);
        //System.out.println("Lever = " + distanceLever.getValue());

    });
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Tasks - Зададия
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>Инициализация заданий</b><br>
     * Заполняется список задач {@code tasks}<br>
     */
    static void botProcessInit() {
   //     BotProcess.init(task);
//        BotProcess.init(testTask);
    }

    /*****************************************************
     * Мигание светодиода                                *
     *****************************************************/
    static String taskNote = "Тестирование датчиков";
    static BotTask task = new BotTask(
            taskNote, true,
        () -> { // Расчёт задержки перед следующим запуском задания
            long delay = delayLever.getValue()*1000;
            return delay;
        },
        () -> { // Задание
            if (!Devices.USED) return;
            try {
                for (Device device: Devices.list) {
                    device.dataRead();
                    Thread.sleep(1);
                }
            } catch (Exception exception) {
                Journal.add(taskNote + ": Задание прервано", NoteType.WRINING);
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
        }
    );

}
