package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

/**
 * <b>Cooling device</b><br>
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

    public CoolingDevice(Pin pin, double temperature) {
        init(pin, temperature, ValueMode.TEMPORARY);
    }

    public CoolingDevice(Pin pin, double temperature, ValueMode valueMode) {
        init( pin, temperature, valueMode);
    }

    private void init(Pin pin, double temperature, ValueMode valueMode) {
        model = "Fan relay"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        note = "CPU cooling fan relay"; // Описание устройства (например, "Сонар для измерения уровня воды")
        name = "coolingDevice"; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.fanRelay = new BooleanValue(name + "_fanrelay", "Реле вентилятора", valueMode, false);
        this.temperature = new DoubleValue(name + "_temperature", "Температура CPU, C", valueMode, 2, temperature);
        values.add(this.fanRelay);
        values.add(this.temperature);
        if (pin == null) return;
        putPin(pin, "");
    }

    @Override
    public boolean initPins() {
        if (!Devices.USED) return false;
        if (getPins().size() == 0) return false;
        gpioPin = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0), getName(), PinState.HIGH);
        gpioPin.setShutdownOptions(true, PinState.LOW);
        coolingThread.start();
        return true;
    }

    private double getTemperature(){
        try {
            FileInputStream fstream = new FileInputStream("/sys/class/thermal/thermal_zone0/temp");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            return Integer.parseInt(br.readLine())/1000d;
        }
        catch(Exception exception){
            exception.printStackTrace();
            return temperature.getValue();
        }
    }

    private Thread coolingThread = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(10000);
                double t = getTemperature();
                System.out.println("Temperature = " + t );
                temperature.setValue(t);
                fanRelay.setValue(t > temperature.getDefaultValue());
                if(!Devices.USED) continue;
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

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String toString(){
        return model;
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}