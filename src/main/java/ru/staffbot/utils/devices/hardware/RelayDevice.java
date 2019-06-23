package ru.staffbot.utils.devices.hardware;

import com.pi4j.io.gpio.*;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.DevicePin;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.values.BooleanValue;
import ru.staffbot.utils.values.Value;

import java.util.ArrayList;

import static javax.management.Query.value;

/**
 * <b>Переключатель</b> позволяет работать с реле, светодиодами и т.п.<br>
 */
public class RelayDevice extends Device {

    private GpioPinDigitalOutput gpioPin;

    private BooleanValue value;

    public RelayDevice(String name, String note, Boolean value, Pin pin) {
        init(name, note, value, pin, true);
    }

    public RelayDevice(String name, String note, Boolean value, Pin pin, Boolean dbStorage) {
        init(name, note, value, pin, dbStorage);
    }

    private void init(String name, String note, Boolean value, Pin pin, Boolean dbStorage) {

        this.model = "Реле"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.value = new BooleanValue(name, note, value, dbStorage);

        values.add(this.value);
        Devices.putToPins(pin, new DevicePin(name));

//        this.value.trueValue = "<input type=\"checkbox\" checked disabled>";
//        this.value.falseValue = "<input type=\"checkbox\" disabled>";

        if(!Devices.USED)return;
        gpioPin = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0), getName(), PinState.LOW);
        gpioPin.setShutdownOptions(true, PinState.LOW);
    }

    public Boolean set(Boolean value) {
        Boolean acceptedValue = this.value.setValue(value);
        if (!Devices.USED) return acceptedValue;
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
    public String getValueAsString(){
        return value.getValueAsString();
    }

}