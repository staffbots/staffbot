package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.*;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.SystemInformation;
import ru.staffbots.tools.devices.drivers.i2c.I2CBusDevice;
import java.util.ArrayList;

/**
 * Devices set, singleton class
 */
public class Devices extends ArrayList<Device> {

    private Devices() {
        super(0);
        if (CoolingDevice.used())
            add(CoolingDevice.getInstance());
    }

    private static boolean addDevice(Device device) {
        if (device == null) return false;
        if (instance.contains(device)) return false;
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
            device.dbInit();
            instance.add(device);
        }
        return !overlap;
    }

    private static final Devices instance = new Devices();

    private static GpioController getController(){
        try {
            if (!SystemInformation.isRaspbian) throw new Exception("Need Raspbian - operation system for Raspberry Pi");
            return GpioFactory.getInstance();
        } catch (Exception e) {
            //Devices.USED = false;
            return null;
        }
    }


    public static GpioController gpioController = getController();

    public static ArrayList<Device> getList() {
        return instance;
    }

    public static ArrayList<Pin> getPins(){
        ArrayList<Pin> result = new ArrayList(0);
        for (Device device: instance)
            result.addAll(device.getPins());
        return result;
    }

    public static int getI2CBusAddress(Pin pin) {
        for (Device device: instance)
            if (device.getPins().contains(pin)) {
                I2CBusDevice busDevice = I2CBusDevice.convertDevice(device);
                if (busDevice != null)
                    return busDevice.getBusAddress();
            }
        return -1;
    }

    public static void addDevices(Device... devices) {
        if (devices == null)
            return;
        for (Device device: devices)
            instance.addDevice(device);
        if (devices.length > 0)
            Journal.add("init_device");
    }

    public static void reset(){
        for (Device device: instance)
            device.reset();
    }

}
