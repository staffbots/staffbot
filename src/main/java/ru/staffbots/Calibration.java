package ru.staffbots;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.ASPHProbeDevice;
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

    static ASPHProbeDevice pHProbeDevice = new ASPHProbeDevice("pHProbe",
            "pH показатель");

    static void devicesInit() {
        Devices.init(pHProbeDevice);
    }

    /////////////////////////////////////////////////////////////
    // Рычаги управления
    /////////////////////////////////////////////////////////////
    static GroupLever label = new GroupLever("label");
    static LongLever bytesLabel = new LongLever("bytesLabel","Количество байтов", 1,40,1024);

    static ButtonLever buttonLever = new ButtonLever("buttonLever",
            "Выполнить","Калибровка датчика, методом триангуляции континума",
            () -> {
                // Обработка нажатия кнопки
                String note;
                try {
                    final byte TSL2561_REG_ID = (byte)0x8A;
                    final byte TSL2561_REG_DATA_0 = (byte)0x8C;
                    final byte TSL2561_REG_DATA_1 = (byte)0x8E;
                    final byte TSL2561_REG_CONTROL = (byte)0x80;
                    final byte TSL2561_POWER_UP = (byte)0x03;
                    final byte TSL2561_POWER_DOWN = (byte)0x00;

                    byte[] bytes = new byte[(int)bytesLabel.getValue()];
                    pHProbeDevice.device.write("R".getBytes(StandardCharsets.US_ASCII));
                    Thread.sleep(1000);
                    int response = pHProbeDevice.device.read(bytes,0,(int) bytesLabel.getValue());
                    note = "Прочитано " +  response + " байтов: " +
                            new String(bytes, StandardCharsets.US_ASCII);
 //                   note = "Прочитано " +  response + " байтов: " + Arrays.toString(bytes);
                    label.setNewNote(note);
//                    System.out.println("BUS_0 = " + I2CBus.BUS_0);
                } catch (Exception exception) {
                    note = "I/O error during fetch of I2C busses occurred";
                }
                label.setNewNote(note);
                //label.setNewNote("dsfdsda");
                //pHProbeDevice.
                Journal.add(note);
            });

    static void leversInit() {
        Levers.init(bytesLabel, buttonLever, label);
        Journal.add("Рычаги управления успешно проинициализированы");
    }


    /////////////////////////////////////////////////////////////
    // Задания автоматизации
    /////////////////////////////////////////////////////////////

    static void tasksInit() {
        Tasks.init();
    }

}
