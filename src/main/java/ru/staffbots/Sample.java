package ru.staffbots;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo.BoardType;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.levers.DoubleLever;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.devices.drivers.LedDevice;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;

/**
 * Простейший пример с миганием светодиода на пине {@code GPIO_01}
 */
public class Sample extends Staffbot {

    // Точка входа приложения
    public static void main(String[] args) {
        Device[] devices = {ledDevice}; // Инициализируем список устройств
        Lever[] levers = {frequencyLever}; // Инициализируем список элементов управления
        Task[] tasks = {ledFlashingTask}; // Инициализируем список заданий
        initiateSolution(devices, levers, tasks);
    }

    static {
        setBoardType(BoardType.RaspberryPi_3B);
        setSolutionName(MethodHandles.lookup().lookupClass().getSimpleName()); // current class name
    }

    /////////////////////////////////////////////////////////////
    // Рычаги управления
    /////////////////////////////////////////////////////////////

    static DoubleLever frequencyLever = new DoubleLever("frequency",
            "Частота мигания светодиода, Гц", ValueMode.TEMPORARY, 2, 0.25);

    /////////////////////////////////////////////////////////////
    // Переферийные устройства
    /////////////////////////////////////////////////////////////

    static LedDevice ledDevice = new LedDevice("led",
            "Светодиод",ValueMode.TEMPORARY, RaspiPin.GPIO_27, false);

   /////////////////////////////////////////////////////////////
    // Задания автоматизации
    /////////////////////////////////////////////////////////////

    static Task ledFlashingTask = new Task( "Мигание светодиода",
            () -> {// Расчёт задержки перед следующим запуском задания в миллисекундах
                return Math.round(500 / frequencyLever.getValue());
            },
            () -> {// Команды выполнения задания
                ledDevice.set(true); // Включаем светодиод
                try { Thread.sleep(Math.round(500/frequencyLever.getValue())); } // Ждём
                catch (Exception exception) {
                    Journal.addAnyNote(NoteType.WARNING, "Мигание светодиода прервано");
                }
                ledDevice.set(false); // Выключаем светодиод
            }
    );

}