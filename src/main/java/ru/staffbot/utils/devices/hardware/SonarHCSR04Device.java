package ru.staffbot.utils.devices.hardware;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.DevicePin;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.values.DoubleValue;


// Сонар
public class SonarHCSR04Device extends Device {

    private GpioPinDigitalOutput gpioPinTRIG;
    private GpioPinDigitalInput gpioPinECHO;
    private DoubleValue value;

    public SonarHCSR04Device(String name, String note, Pin pinTRIG, Pin pinECHO) {
        init(name, note, pinTRIG, pinECHO, true);
    }

    public SonarHCSR04Device(String name, String note, Pin pinTRIG, Pin pinECHO, Boolean dbStorage) {
        init(name, note, pinTRIG, pinECHO, dbStorage);
    }

    private void init(String name, String note, Pin pinTRIG, Pin pinECHO, Boolean dbStorage) {

        this.model = "Сонар HC-SR04"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.value = new DoubleValue(name, "", dbStorage);

        values.add(this.value);
        Devices.putToPins(pinTRIG, new DevicePin(name,"TRIG"));
        Devices.putToPins(pinECHO, new DevicePin(name,"ECHO"));

        if(!Devices.USED)return;
        gpioPinTRIG = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0));
        gpioPinECHO = Devices.gpioController.provisionDigitalInputPin(getPins().get(1));

    }


    public double get() {
        //измерение
        return 0.0;
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String getValueAsString(){
        return this.value.getValueAsString();
        //return this.value.getValue() ? "Включен" : "Выключен";
    }

}
