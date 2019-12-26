package ru.staffbots;

import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.ECProbeI2CBusDevice;
import ru.staffbots.tools.devices.drivers.UARTProbeDevice;
import ru.staffbots.tools.levers.*;
import ru.staffbots.tools.tasks.Tasks;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static orangepi.I2CExample.TSL2561_REG_ID;

public class Calibration extends Pattern {

    // Точка входа приложения
    public static void main(String[] args) {
        solutionName = new Object(){}.getClass().getEnclosingClass().getSimpleName();
        solutionInit(()->{
            Levers.init(buttonLever); // Инициализируем список элементов управления
            Devices.init(probeDevice); // Инициализируем список устройств
            Tasks.init(); // Инициализируем список заданий
        });
    }
    /////////////////////////////////////////////////////////////
    // Переферийные устройства
    /////////////////////////////////////////////////////////////

    static ECProbeI2CBusDevice probeDevice = new ECProbeI2CBusDevice("ProbeDevice",
            "Датчик EC", 1, 100);
    //static UARTProbeDevice uartProbeDevice = new UARTProbeDevice("ProbeDevice","Датчик");


    /////////////////////////////////////////////////////////////
    // Рычаги управления
    /////////////////////////////////////////////////////////////
    static ButtonLever buttonLever = new ButtonLever("buttonLever",
            "Выполнить","Калибровка датчика, методом триангуляции континума",
            () -> {
                // Обработка нажатия кнопки
                try {
                    System.out.println("EC = " + probeDevice.getConductivity());
                    System.out.println("TDS = " + probeDevice.getTotalDissolvedSolids(false));
                    System.out.println("SAL = " + probeDevice.getSalinity(false));
                    System.out.println("SG = " + probeDevice.getSpecificGravity(false));
                } catch (Exception exception) {
                    Journal.add("", NoteType.ERROR);
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
