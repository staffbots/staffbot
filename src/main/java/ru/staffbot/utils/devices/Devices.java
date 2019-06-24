package ru.staffbot.utils.devices;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.utils.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Devices{

    /**
     * <b>Список устройств</b>,
     * используется для групповой обработки в StatusServlet
     */
    public static ArrayList<Device> list = new ArrayList<Device>(0);

    /**
     * Массив пинов с привязкой к пину на этом устройстве
     */
    public static Map<Pin, DevicePin> pins = new HashMap();

    public static boolean putToPins(Pin pin, DevicePin devicePin){
        if (pins.containsKey(pin)) {
            System.out.println("Нельзя использовать один и тот же пин (" + pin + ") несколько раз!");
            System.exit(0);
        }
        pins.put(pin, devicePin);
        return true;
    }

    public static Boolean USED = true;

    /**
     * @return возвращает true в случае если программа запущена в операционной системе Raspbian
     * (то есть на контроллере Raspberry Pi), иначе - false
     */
    public static boolean isRaspbian() {
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        return operatingSystem.contains("linux");
    }

    public static GpioController gpioController = getController();

    private static GpioController getController(){
        try {
            if (!isRaspbian()) throw new Exception("Программа запущена не на контроллере Raspberry Pi");
            return GpioFactory.getInstance();
        } catch (Exception e) {
            Devices.USED = false;
            return null;
        }

    }

    public static void init(Device... devices) {
        list.clear();
        for (Device device : devices) {
            list.add(device);
            for (Value value : device.getValues())
                if (value.isStorable())
                    value.createTable();
        }
        Journal.add("Устройства успешно проинициализированы");
    }

    public static void reset(){
        for (Device device: Devices.list)
            device.reset();
    }

}
