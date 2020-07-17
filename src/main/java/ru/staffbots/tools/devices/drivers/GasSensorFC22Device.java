package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.gpio.PinState;
import ru.staffbots.Staffbot;
import ru.staffbots.tools.SystemInformation;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;

import java.lang.invoke.MethodHandles;

public class
GasSensorFC22Device extends Device {

    @Override
    public boolean initPins() {
        if (!SystemInformation.isRaspbian) return false;
        if (getPins().size() < 1) return false;
//        gpioPin = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0), getName(), PinState.LOW);
//        gpioPin.setShutdownOptions(true, PinState.LOW);
        return true;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}
