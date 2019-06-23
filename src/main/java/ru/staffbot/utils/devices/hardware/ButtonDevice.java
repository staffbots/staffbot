package ru.staffbot.utils.devices.hardware;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.DevicePin;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.values.BooleanValue;

public class ButtonDevice extends Device {

    private GpioPinDigitalInput gpioPin;
    //private GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);

    private BooleanValue value;

    public ButtonDevice(String name, String note, Pin pin, Runnable action) {
        init(name, note, pin, true, action);
    }

    public ButtonDevice(String name, String note, Pin pin, Boolean dbStorage, Runnable action) {
        init(name, note, pin, dbStorage, action);
    }

    private void init(String name, String note, Pin pin, Boolean dbStorage, Runnable action) {

        this.model = "Кнопка"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.value = new BooleanValue(name, note, dbStorage, false);

        values.add(this.value);
        Devices.putToPins(pin, new DevicePin(name));

//        this.value.trueValue = "<input type=\"checkbox\" checked disabled>";
//        this.value.falseValue = "<input type=\"checkbox\" disabled>";

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
                    action.run();
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
