package ru.staffbots;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.DateAccuracy;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.tools.tasks.TasksStatus;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.*;
import ru.staffbots.tools.levers.*;
import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.values.ValueMode;
import java.lang.invoke.MethodHandles;
import java.util.Date;

public class Tester extends Staffbot {

    // Точка входа при запуске приложения
    public static void main(String[] args) {
        solutionInit(
                MethodHandles.lookup().lookupClass().getSimpleName(), // Имя текущего класса
                new Device[] {ledDevice, sensor, sonar, button}, // Инициализируем список устройств
                new Object[] {"Группа параметров", booleanLever, delayLever, buttonLever, listLever, dateLever}, // Инициализируем список элементов управления
                new Task[] {task, testTask}
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Levers - Рычаги
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static LongLever delayLever = new LongLever("delayLever",
            "Частота опроса, сек", ValueMode.TEMPORARY, 20, 2, 60*60);

    static ButtonLever buttonLever = new ButtonLever("buttonLever",
        "Выполнить","Калибровка датчика, методом триангуляции континума",
        () -> {
        // Обработка нажатия кнопки
        Journal.addAnyNote("Нажата кнопка калибровки датчика");
    });

    static ListLever listLever = new ListLever("listLever",
            "Тестовый список", "строка 0","строка 1","строка 2","строка 3");

    static DateLever dateLever = new DateLever("dateLever",
            "тестовая дата", ValueMode.STORABLE, LeverMode.CHANGEABLE, DateFormat.SHORTTIME, "12:22");

    static BooleanLever booleanLever = new BooleanLever("booleanLever",
            "я гениален", ValueMode.STORABLE, false);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Devices - Устройства
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static LedDevice ledDevice = new LedDevice("ledDevice",
            "Индикатор конца света", RaspiPin.GPIO_01, false);
    static SensorDHT22Device sensor = new SensorDHT22Device("sensorDevice",
            "Датчик температуры и влажности", RaspiPin.GPIO_25);
    static SonarHCSR04Device sonar = new SonarHCSR04Device("sonarDevice",
        "Расстояние до врага", RaspiPin.GPIO_04, RaspiPin.GPIO_05);
    static ButtonDevice button = new ButtonDevice("buttonDevice",
        "Ракетно ядерный залп", ValueMode.TEMPORARY, RaspiPin.GPIO_06,
        () -> {// Обработка нажатия кнопки
            //sensor.dataRead();
            Journal.addAnyNote("!!! Temperature = " + sensor.getTemperature());
            Journal.addAnyNote("!!! Humidity = " + sensor.getHumidity());
            //System.out.println("Distance = " + sonar.getDistance());
        }
    );
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Tasks - Зададия
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*****************************************************
     * Мигание светодиода                                *
     *****************************************************/
    static String taskNote = "Тестирование датчиков";
    static Task task = new Task(
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
                Journal.addAnyNote(NoteType.WARNING,taskNote + ": Задание прервано");
            }
        }
    );

    /*****************************************************
     * Заполнение БД тестовыми случайными значениями                              *
     *****************************************************/
    static String testTaskNote = "Заполнение БД";
    static Task testTask = new Task(
        testTaskNote,
        () -> { // Расчёт задержки перед следующим запуском задания
            long delay = 0;
            return delay;
        },
        () -> { // Задание без повторений
            long timePeriod = DateAccuracy.DAY.getMilliseconds();
            Period period = new Period(DateFormat.DATE, new Date(System.currentTimeMillis() - timePeriod), new Date());
            for (Device device : Devices.list)
                for (Value value : device.getValues())
                    value.setRandom(period);
            for (Lever lever : Levers.list)
                lever.toValue().setRandom(period);
            Tasks.setStatus(TasksStatus.STOP);
        }
    );

}
