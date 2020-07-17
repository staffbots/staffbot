package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import ru.staffbots.Staffbot;
import ru.staffbots.tools.SystemInformation;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

/**
 * <b>Cooling device</b>, singleton class<br>
 * Integrated device for CPU cooling<br>
 * The device consists of a fan connected via a NPN-transistor to a standard GPIO. <br>
 * The only class instance is created at the time of loading the parameters (see cfg-file): <br>
 * <em>pi.fanpin</em> - GPIO pin number of a cooling fan NPN-transistor. Cooling is disable, if this number < 0 <br>
 * <em>pi.temperature</em> - temperature value when the cooling fan turns on. Used only when cooling is on (pi.fanpin > -1) <br>
 **/
public class CoolingDevice extends Device {

    private GpioPinDigitalOutput gpioPin = null;

    private BooleanValue fanRelay;

    private DoubleValue temperature;

    private CoolingDevice() {}

    private Thread coolingThread = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(10000);
                double t = SystemInformation.getCPUTemperature(temperature.getValue());
                System.out.println("Temperature = " + t );
                temperature.setValue(t);
                fanRelay.setValue(t > temperature.getDefaultValue());
                if(!SystemInformation.isRaspbian) continue;
                if (fanRelay.getValue()) {
                    gpioPin.high();
                } else {
                    gpioPin.low();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                break;
            }
        }
    }, "Cooling thread");

    @Override
    public boolean initPins() {
        if (!SystemInformation.isRaspbian) return false;
        if (getPins().size() == 0) return false;
        gpioPin = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0), getName(), PinState.HIGH);
        gpioPin.setShutdownOptions(true, PinState.LOW);
        coolingThread.start();
        return true;
    }

    @Override
    public String toString(){
        return model;
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

    private static final CoolingDevice instance = new CoolingDevice();

    public static CoolingDevice getInstance() {
        return instance;
    }

    public static void init(Pin pin, double temperature) {
        init(pin, temperature, ValueMode.TEMPORARY);
    }

    public static void init(Pin pin, double temperature, ValueMode valueMode) {
        instance.model = "Fan relay"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        instance.note = "CPU cooling fan relay"; // Описание устройства (например, "Сонар для измерения уровня воды")
        instance.name = "coolingDevice"; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        instance.fanRelay = new BooleanValue(instance.name + "_fanrelay", "Реле вентилятора", valueMode, false);
        instance.temperature = new DoubleValue(instance.name + "_temperature", "Температура CPU, C", valueMode, 2, temperature);
        instance.values.add(instance.fanRelay);
        instance.values.add(instance.temperature);
        if (pin == null) return;
        instance.putPin(pin, "");
    }

    public static boolean used() {
        return SystemInformation.isRaspbian && (getInstance().getPins().size() > 0);
    }
}