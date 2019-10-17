package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.io.IOException;

/**
 * https://www.atlas-scientific.com/product_pages/kits/ph-kit.html
 */
public class ASPHProbeDevice extends Device {

    public I2CDevice device;
    // Точность - количество знаков после запятой
    private final static int ACCURACY = 3;

    private int address = 99;

    private DoubleValue value;

    public ASPHProbeDevice(String name, String note) {
        init(name, note, ValueMode.STORABLE, address);
    }

    public ASPHProbeDevice(String name, String note, int address) {
        init(name, note, ValueMode.STORABLE, address);
    }

    public ASPHProbeDevice(String name, String note, ValueMode valueMode, int address) {
        init(name, note, valueMode, address);
    }

    private void init(String name, String note, ValueMode valueMode, int address) {

        this.model = "Датчик pH"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.address = address;
        this.value = new DoubleValue(name, "Водородный показатель", valueMode, ACCURACY);

        values.add(this.value);

//        Devices.putToPins(pinTRIG, new DevicePin(name, "TRIG"));
//        Devices.putToPins(pinECHO, new DevicePin(name, "ECHO"));

        if (!Devices.USED) return;

        try {
            I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
            device = i2c.getDevice(address);
        } catch (I2CFactory.UnsupportedBusNumberException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        gpioPinTRIG = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0));
//        gpioPinECHO = Devices.gpioController.provisionDigitalInputPin(getPins().get(1));
    }


    @Override
    public String getModel(boolean byClassName) {
        if(!byClassName) return model;
        String className = (new Object(){}.getClass().getEnclosingClass().getSimpleName());
        return className.substring(0, className.length() - 6);
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
