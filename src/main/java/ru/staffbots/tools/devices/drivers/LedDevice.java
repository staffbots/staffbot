package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import ru.staffbots.Staffbot;
import ru.staffbots.tools.SystemInformation;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;

/**
 * <b>Переключатель</b> позволяет работать с реле, светодиодами и т.п.<br>
 */
public class LedDevice extends Device {

    private GpioPinDigitalOutput gpioPin;

    private BooleanValue value;

    public LedDevice(String name, String note, Pin pin, Boolean value) {
        init(name, note, ValueMode.STORABLE, pin, value);
    }

    public LedDevice(String name, String note, ValueMode valueMode, Pin pin, Boolean value) {
        init(name, note, valueMode, pin, value);
    }

    private void init(String name, String note, ValueMode valueMode, Pin pin, Boolean value) {
        this.model = "LED"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.value = new BooleanValue(name, note, valueMode, value);
        values.add(this.value);
        putPin(pin, "");
    }

    @Override
    public boolean initPins() {
        if (!SystemInformation.isRaspbian) return false;
        if (getPins().size() < 1) return false;
        gpioPin = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0), getName(), PinState.LOW);
        gpioPin.setShutdownOptions(true, PinState.LOW);
        return true;
    }

    public Boolean set(Boolean value) {
        Boolean acceptedValue = this.value.setValue(value);
        if (!SystemInformation.isRaspbian) return acceptedValue;
        if (value) {
            gpioPin.high();
        } else {
            gpioPin.low();
        }
        return acceptedValue;
    }

    public Boolean get() {
        return value.getValue();
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String toString(){
        return value.toString();
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}