package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.DevicePin;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.ValueMode;

public class ButtonDevice extends Device {

    private GpioPinDigitalInput gpioPin;
    //private GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);

    private BooleanValue value;

    public ButtonDevice(String name, String note, Pin pin, Runnable action) {
        init(name, note, ValueMode.STORABLE, pin, action);
    }

    public ButtonDevice(String name, String note, ValueMode valueMode, Pin pin, Runnable action) {
        init(name, note, valueMode, pin, action);
    }

    private void init(String name, String note, ValueMode valueMode, Pin pin, Runnable actionOnClick) {

        this.model = "Кнопка"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.value = new BooleanValue(name, note, valueMode, false);

        values.add(this.value);
        Devices.putToPins(pin, new DevicePin(name));

        if(!Devices.USED)return;
        gpioPin = Devices.gpioController.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);
        //gpioPin.setShutdownOptions(true, PinState.LOW);
        gpioPin.setShutdownOptions(true);

        gpioPin.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                value.setValue(event.getState() == PinState.HIGH);
                if (value.getValue())
                    actionOnClick.run();
            }

        });
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
