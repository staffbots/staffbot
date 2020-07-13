package ru.staffbots;

import com.pi4j.system.SystemInfo;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.dates.DateAccuracy;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.devices.drivers.network.AddressSettings;
import ru.staffbots.tools.devices.drivers.network.grower.MainESP32Device;
import ru.staffbots.tools.devices.drivers.network.grower.RegularESP32Device;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.tools.tasks.TasksStatus;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.levers.*;
import ru.staffbots.tools.values.Value;
import java.lang.invoke.MethodHandles;
import java.util.Date;

public class Tester extends Staffbot {

    // Точка входа при запуске приложения
    public static void main(String[] args) {
        initiateSolution(
//                new Device[] {regularDevice, bh1750Device, distanceDevice, sensorDevice, buttonDevice}, // Инициализируем список устройств
//                new Lever[] {buttonLever, distanceLever}, // Инициализируем список элементов управления
                new Device[] {mainDevice, regularDevice}, // Инициализируем список устройств
                new Lever[] {buttonLever}, // Инициализируем список элементов управления
                new Task[] {}
        );
    }

    static {
        setBoardType(SystemInfo.BoardType.RaspberryPi_3B);
        setSolutionName(MethodHandles.lookup().lookupClass().getSimpleName()); // current class name
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //  Levers - Рычаги
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*    static LedDevice ledDevice = new LedDevice("ledDevice",
            "Светодиод", ValueMode.TEMPORARY, RaspiPin.GPIO_29, false);

    static DoubleLever distanceLever = new DoubleLever("distanceLever",
            "Расстояние", LeverMode.OBSERVABLE, 3);

    static SonarHCSR04Device distanceDevice = new SonarHCSR04Device("distanceDevice",
            "Расстояние", ValueMode.TEMPORARY, RaspiPin.GPIO_25, RaspiPin.GPIO_28);

    static BH1750Device bh1750Device = new BH1750Device("bh1750Device",
            "Уровень освещения", 1);

    static SensorDHT22Device sensorDevice = new SensorDHT22Device("sensorDevice",
           "Датчик температуры и влажности", RaspiPin.GPIO_04);
*/
    static RegularESP32Device regularDevice =
        new RegularESP32Device(new AddressSettings("10.10.10.200"), "regularDevice", "First ESP32");

    static MainESP32Device mainDevice =
            new MainESP32Device(new AddressSettings("10.10.10.201"), "mainDevice", "Main ESP32");

    static Runnable buttonClickAction = () -> {
        mainDevice.setEcPumpRelay(!mainDevice.getEcPumpRelay());
        mainDevice.setPhUpPumpRelay(!mainDevice.getPhUpPumpRelay());
        mainDevice.setPhDownPumpRelay(!mainDevice.getPhDownPumpRelay());
        System.out.println("Aqua temperature: " + mainDevice.getAquaTemperature());
        System.out.println("pH probe: " + mainDevice.getPhProbe());
        System.out.println("EC probe: " + mainDevice.getEcProbe());
//        regularDevice.setValveRelay(!regularDevice.getValveRelay());
        System.out.println(regularDevice.getLightLevel());
        System.out.println(regularDevice.getAirTemperature());
        System.out.println(regularDevice.getAirHumidity());
//        System.out.println(regularDevice.getSoilMoisture());
    };

    static ButtonLever buttonLever = new ButtonLever("buttonLever",
        "Measuring","Кнопка интефейса",
            buttonClickAction
    );
/*
    static ButtonDevice buttonDevice = new ButtonDevice("buttonDevice",
        "Физическая кнопка", ValueMode.TEMPORARY, RaspiPin.GPIO_06,
            buttonClickAction
    );
*/
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

            if (!Devices.isRaspbian) return;
            try {
                for (Device device: Devices.getList()) {
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
            for (Device device : Devices.getList())
                for (Value value : device.getValues())
                    value.setRandom(period);
            for (Lever lever : Levers.getList())
                lever.toValue().setRandom(period);
            Tasks.setStatus(TasksStatus.STOP);
        }
    );

}
