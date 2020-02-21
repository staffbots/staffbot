package ru.staffbots;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.ECProbeI2CBusDevice;
import ru.staffbots.tools.devices.drivers.LedDevice;
import ru.staffbots.tools.levers.*;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;


public class Calibration extends Staffbot {

    // Точка входа приложения
    public static void main(String[] args) {
        solutionInit(
                SystemInfo.BoardType.RaspberryPi_3B,
                MethodHandles.lookup().lookupClass().getSimpleName(), // Имя текущего класса
                new Device[] {probeDevice, ledDevice, probeDevice1}, // Инициализируем список устройств
                new Lever[] {buttonLever}, // Инициализируем список элементов управления
                null
        );
    }
    /////////////////////////////////////////////////////////////
    // Переферийные устройства
    /////////////////////////////////////////////////////////////

    static ECProbeI2CBusDevice probeDevice = new ECProbeI2CBusDevice("probeDevice",
            "Датчик EC", 1, 100);
    static ECProbeI2CBusDevice probeDevice1 = new ECProbeI2CBusDevice("probeDevice1",
            "Датчик EC", 1, 102);
    static LedDevice ledDevice = new LedDevice("ledDevice",
            "Светодиод", ValueMode.STORABLE, RaspiPin.GPIO_07, false);

    //static UARTProbeDevice uartProbeDevice = new UARTProbeDevice("ProbeDevice","Датчик");


    /////////////////////////////////////////////////////////////
    // Рычаги управления
    /////////////////////////////////////////////////////////////
    static ButtonLever buttonLever = new ButtonLever("button",
            "Выполнить","Калибровка датчика, методом триангуляции континума",
            () -> {
                // Обработка нажатия кнопки
                try {
                    System.out.println("EC = " + probeDevice.getConductivity());
                    System.out.println("TDS = " + probeDevice.getTotalDissolvedSolids(false));
                    System.out.println("SAL = " + probeDevice.getSalinity(false));
                    System.out.println("SG = " + probeDevice.getSpecificGravity(false));
                } catch (Exception exception) {
                    Journal.addAnyNote(NoteType.ERROR, exception.getMessage());
                    //note = "I/O error during fetch of I2C busses occurred";
                }
                //System.out.println(note);
                //label.setNote(note.replaceAll("\n","<br>"));
                //Journal.add(note);
            });


    /////////////////////////////////////////////////////////////
    // Задания автоматизации
    /////////////////////////////////////////////////////////////


}
