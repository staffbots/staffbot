package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

/**
 * <b>Cooling device</b><br>
 **/
public class CoolingDevice extends Device {

    private GpioPinDigitalOutput gpioPin = null;

    private BooleanValue fanRelay;

    private DoubleValue temperature;

    public CoolingDevice(Pin pin, int temperature) {
        init(pin, temperature, ValueMode.TEMPORARY);
    }

    public CoolingDevice(Pin pin, int temperature, ValueMode valueMode) {
        init( pin, temperature, valueMode);
    }

    private void init(Pin pin, int temperature, ValueMode valueMode) {

        model = "Fan relay"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        note = "CPU cooling fan relay"; // Описание устройства (например, "Сонар для измерения уровня воды")
        name = "coolingDevice"; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.fanRelay = new BooleanValue(name + "_fanrelay", "Реле вентилятора", valueMode, false);
        this.temperature = new DoubleValue(name + "_temperature", "Температура CPU, C", valueMode, 2, (double)temperature);
        values.add(this.fanRelay);
        values.add(this.temperature);
        if (pin == null) return;
        putPin(pin, "");
        if (!Devices.USED) return;
        try {
            gpioPin = Devices.gpioController.provisionDigitalOutputPin(pin, getName(), PinState.HIGH);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        gpioPin.setShutdownOptions(true, PinState.LOW);
        coolingThread.start();
    }

    public boolean coolingRunnable(){
        return  ( coolingThread.getState() == Thread.State.RUNNABLE ) ||
                ( coolingThread.getState() == Thread.State.WAITING ) ||
                ( coolingThread.getState() == Thread.State.TIMED_WAITING );
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