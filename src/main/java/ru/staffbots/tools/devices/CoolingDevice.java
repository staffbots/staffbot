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

import java.lang.invoke.MethodHandles;

/**
 * <b>Cooling device</b><br>
 **/
public class CoolingDevice extends Device {

    private GpioPinDigitalOutput gpioPin = null;

    private BooleanValue fanRelay;

    private DoubleValue temperature;

    public CoolingDevice(Pin pin, int temperature) {
        init(pin, temperature, ValueMode.STORABLE);
    }

    public CoolingDevice(Pin pin, int temperature, ValueMode valueMode) {
        init( pin, temperature, valueMode);
    }

    private void init(Pin pin, int temperature, ValueMode valueMode) {

        model = "Relay"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        note = "Cooling"; // Описание устройства (например, "Сонар для измерения уровня воды")
        name = "coolingDevice"; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")


        this.fanRelay = new BooleanValue(name + "_fanrelay", "Реле включения вентилятора", valueMode, false);
        this.temperature = new DoubleValue(name + "_temperature", "Температура CPU, C", valueMode, 2, (double)temperature);
        values.add(this.fanRelay);
        values.add(this.temperature);

        putPin(pin, "");

        if(!Devices.USED)return;
        gpioPin = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0), getName(), PinState.LOW);
        gpioPin.setShutdownOptions(true, PinState.LOW);
    }


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