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
        // ВНИМАНИЕ! Порядок инициализаций менять не рекомендуется
        // Определяем наименование решения по названию текущего класса
        solutionName = new Object(){}.getClass().getEnclosingClass().getSimpleName();
        propertiesInit(); // Загружаем свойства из cfg-файла
        databaseInit(); // Подключаемся к базе данных
        leversInit(); // Инициализируем список элементов управления
        devicesInit(); // Инициализируем список устройств
        tasksInit(); // Инициализируем список заданий
        webserverInit(); // Запускаем веб-сервер
        windowInit(); // Открываем главное окно приложения
    }
    /////////////////////////////////////////////////////////////
    // Переферийные устройства
    /////////////////////////////////////////////////////////////

    static ECProbeI2CBusDevice probeDevice = new ECProbeI2CBusDevice("ProbeDevice",
            "Датчик EC", 1, 100);
    //static UARTProbeDevice uartProbeDevice = new UARTProbeDevice("ProbeDevice","Датчик");

    static void devicesInit() {
        Devices.init(probeDevice);
    }

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

    static void leversInit() {
        Levers.init(buttonLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }


    /////////////////////////////////////////////////////////////
    // Задания автоматизации
    /////////////////////////////////////////////////////////////

    static void tasksInit() {
        Tasks.init();
    }

}
