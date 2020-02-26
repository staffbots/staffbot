package ru.staffbots;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo;
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
                SystemInfo.BoardType.RaspberryPi_3B,
                MethodHandles.lookup().lookupClass().getSimpleName(), // Имя текущего класса
                new Device[] {esp32Device,bh1750Device, ledDevice, distanceDevice, sensorDevice, buttonDevice}, // Инициализируем список устройств
                new Lever[] {buttonLever, distanceLever}, // Инициализируем список элементов управления
                new Task[] {}
        );
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Levers - Рычаги
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static LedDevice ledDevice = new LedDevice("ledDevice",
            "Светодиод", ValueMode.TEMPORARY, RaspiPin.GPIO_29, false);

    static DoubleLever distanceLever = new DoubleLever("distanceLever",
            "Расстояние", LeverMode.OBSERVABLE, 3);

    static SonarHCSR04Device distanceDevice = new SonarHCSR04Device("distanceDevice",
            "Расстояние", ValueMode.TEMPORARY, RaspiPin.GPIO_25, RaspiPin.GPIO_28);

    static BH1750Device bh1750Device = new BH1750Device("bh1750Device",
            "Освещение", 1, PinState.HIGH);

    static SensorDHT22Device sensorDevice = new SensorDHT22Device("sensorDevice",
           "Датчик температуры и влажности", RaspiPin.GPIO_04);

    static RegularESP32Device esp32Device = new RegularESP32Device("esp32Device", "Первый ESP32");

    static Runnable buttonClickAction = () -> {
        ledDevice.set(!ledDevice.get());
        distanceLever.setValue(distanceDevice.getDistance());
        bh1750Device.getLightIntensity();
        System.out.println(esp32Device.getLightIntensity());
        System.out.println(esp32Device.post("esp32Device_led=23.2"));
        //Journal.addAnyNote("!!! Temperature = " + sensor.getTemperature());
        //Journal.addAnyNote("!!! Humidity = " + sensor.getHumidity());
    };

    static ButtonLever buttonLever = new ButtonLever("buttonLever",
        "Кнопка","Кнопка интефейса",
            buttonClickAction
    );

    static ButtonDevice buttonDevice = new ButtonDevice("buttonDevice",
        "Физическая кнопка", ValueMode.TEMPORARY, RaspiPin.GPIO_06,
            buttonClickAction
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
            long delay = 5000;//delayLever.getValue()*1000;
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
