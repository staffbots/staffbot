package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.devices.drivers.general.I2CBusDevice;
import ru.staffbots.tools.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Устройства
 */
public class Devices{


    /*
     * Парамер включения/отключения gpio-библиотек для контроллера Raspberry Pi:
     * true - gpio-библиотеки включены, используется для боевой компиляции на контроллере
     * false - gpio-библиотеки отключены, устанавливается автоматически, если операционная система отлична от Raspbian
     * Значение можно выставить в файле staffbot.cfg параметром pi.used
     */
    public static Boolean USED = true;

    /**
     * <b>Список устройств</b>,
     * используется для групповой обработки в StatusServlet
     */
    public static ArrayList<Device> list = new ArrayList();

    public static ArrayList<Pin> getPins(){
        ArrayList<Pin> result = new ArrayList(0);
        for (Device device: list)
            result.addAll(device.getPins());
        return result;
    }

    public static int getI2CBusAddress(Pin pin) {
        for (Device device: list)
            if (device.getPins().contains(pin)) {
                I2CBusDevice busDevice = I2CBusDevice.convertDevice(device);
                if (busDevice != null)
                    return busDevice.getBusAddress();
            }
        return -1;
    }

    public static boolean putDevice(Device device) {
        boolean overlap = device.overlap;
        if (!overlap)
            for (Pin pin: getPins())
                if (device.getPins().contains(pin)) {
                    I2CBusDevice busDevice = I2CBusDevice.convertDevice(device);
                    overlap = (busDevice == null) ? true : (busDevice.getBusAddress() == getI2CBusAddress(pin) || (getI2CBusAddress(pin) == -1));
                    if (overlap) break;
                }
        if (overlap)
            Journal.add(NoteType.ERROR, "overlap_pin", device.getName());
        else
            list.add(device);
        return !overlap;
    }

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
            if (!isRaspbian()) throw new Exception("Need Raspbian - operation system for Raspberry Pi");
            return GpioFactory.getInstance();
        } catch (Exception e) {
            Devices.USED = false;
            return null;
        }
    }

    public static void init(Device... devices) {
        list.clear();
        if (devices == null) devices = new Device[0];
        for (Device device: devices) {
            if(list.contains(device)) continue;
            if(putDevice(device))
                for (Value value : device.getValues())
                    if (value.isStorable())
                        value.createTable();
        }
        if (devices.length > 0)
            Journal.add("init_device");
    }

    public static void reset(){
        for (Device device: Devices.list)
            device.reset();
    }

}
