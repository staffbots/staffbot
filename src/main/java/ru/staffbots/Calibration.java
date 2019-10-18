package ru.staffbots;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.I2CProbeDevice;
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

    static I2CProbeDevice probeDevice = new I2CProbeDevice("ProbeDevice",
            "Датчик", 1, 100);
    static UARTProbeDevice uartProbeDevice = new UARTProbeDevice("ProbeDevice","Датчик");

    static void devicesInit() {
        Devices.init(probeDevice);
    }

    /////////////////////////////////////////////////////////////
    // Рычаги управления
    /////////////////////////////////////////////////////////////
    static GroupLever label = new GroupLever("label");

    static ButtonLever buttonLever = new ButtonLever("buttonLever",
            "Выполнить","Калибровка датчика, методом триангуляции континума",
            () -> {
                // Обработка нажатия кнопки
                String note = "nothing";
                try {
                    //uartProbeDevice.write("OK,0");
                    uartProbeDevice.write("Status");
                    //uartProbeDevice.write("i");
                    //uartProbeDevice.write("R");
                    //uartProbeDevice.write("i");
                    //note = "\n";
                    //note += probeDevice.readln("i", 300) + "\n";
                    //note += probeDevice.readln("T,?", 300) + "\n";
                    //note += probeDevice.readln("Status", 300) + "\n";

                } catch (Exception exception) {
                    //note = "I/O error during fetch of I2C busses occurred";
                }
                System.out.println(note);

                label.setNote(note.replaceAll("\n","<br>"));
                Journal.add(note);
            });

    static void leversInit() {
        Levers.init(buttonLever, label);
        Journal.add("Рычаги управления успешно проинициализированы");
    }


    /////////////////////////////////////////////////////////////
    // Задания автоматизации
    /////////////////////////////////////////////////////////////

    static void tasksInit() {
        Tasks.init();
    }

}
