package ru.staffbots;

import com.pi4j.io.gpio.RaspiPin;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.levers.DoubleLever;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.LedDevice;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.values.ValueMode;

/**
 * Простейший пример с миганием светодиода на пине {@code GPIO_01}
 */
public class Staffbot extends Pattern {

    // Точка входа приложения
    public static void main(String[] args) {
        solutionName = new Object(){}.getClass().getEnclosingClass().getSimpleName();
        solutionInit(()->{
            Levers.init(frequencyLever); // Инициализируем список элементов управления
            Devices.init(ledDevice); // Инициализируем список устройств
            Tasks.init(ledFlashingTask); // Инициализируем список заданий
        });
    }

    /////////////////////////////////////////////////////////////
    // Рычаги управления
    /////////////////////////////////////////////////////////////

    static DoubleLever frequencyLever = new DoubleLever("frequency",
            "Частота мигания светодиода, Гц", ValueMode.TEMPORARY, 2, 0.25);

    static void leversInit() {
        Levers.init(frequencyLever);
        Journal.add("Рычаги управления успешно проинициализированы");
    }

    /////////////////////////////////////////////////////////////
    // Переферийные устройства
    /////////////////////////////////////////////////////////////

    static LedDevice ledDevice = new LedDevice("led",
            "Светодиод", RaspiPin.GPIO_01, false);

    static void devicesInit() {
        Devices.init(ledDevice);
    }

    /////////////////////////////////////////////////////////////
    // Задания автоматизации
    /////////////////////////////////////////////////////////////

    static Task ledFlashingTask = new Task( "Мигание светодиода",
            () -> {// Расчёт задержки перед следующим запуском задания в миллисекундах
                return Math.round(500/frequencyLever.getValue());
            },
            () -> {// Команды выполнения задания
                ledDevice.set(true); // Включаем светодиод
                try { Thread.sleep(Math.round(500/frequencyLever.getValue())); } // Ждём
                catch (Exception exception) { Journal.add("Мигание светодиода прервано", NoteType.WRINING); }
                ledDevice.set(false); // Выключаем светодиод
            }
    );


}