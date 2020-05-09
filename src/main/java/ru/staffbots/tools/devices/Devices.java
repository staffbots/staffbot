package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.*;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.devices.drivers.i2c.I2CBusDevice;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Устройства
 */
public class Devices{

    public static CoolingDevice coolingDevice = null;

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
        if (device == null) return false;
        if (list.contains(device)) return false;
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
        else {
            if (!device.initPins()) return false;
            device.initValues();
            list.add(device);
        }
        return !overlap;
    }

    public static final boolean isRaspbian = isRaspbian();
    /**
     * @return возвращает true в случае если программа запущена в операционной системе Raspbian
     * (то есть на контроллере Raspberry Pi), иначе - false
     */
    private static boolean isRaspbian() {
        String osType = System.getProperty("os.name").toLowerCase();
        if (!osType.contains("linux")) return false;
        String osName;
        try (FileInputStream fstream = new FileInputStream("/etc/issue")){
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            osName = br.readLine();
            if (osName == null) return false;
        }
        catch(Exception exception){
            return false;
        }
        return osName.toLowerCase().contains("raspbian");
    }

    public static GpioController gpioController = getController();

    private static GpioController getController(){
        try {
            if (!isRaspbian()) throw new Exception("Need Raspbian - operation system for Raspberry Pi");
            return GpioFactory.getInstance();
        } catch (Exception e) {
            //Devices.USED = false;
            return null;
        }
    }

    public static void init(Device... devices) {
        list.clear();
        if (devices == null) devices = new Device[0];
        for (Device device: devices)
            putDevice(device);
        putDevice(coolingDevice);
        if (devices.length > 0)
            Journal.add("init_device");
    }

    public static void reset(){
        for (Device device: Devices.list)
            device.reset();
    }

}
