package ru.staffbots.tools.devices.drivers;

import ru.staffbots.tools.devices.drivers.I2CBusDevice;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class pHProbeI2CBusDevice extends I2CBusDevice {

    private DoubleValue value;

    public pHProbeI2CBusDevice(String name, String note, int busNumber, int address) {
        super(name, note, busNumber, address);
        init(ValueMode.STORABLE);
    }

    public pHProbeI2CBusDevice(String name, String note, ValueMode valueMode, int busNumber, int address) {
        super(name, note, busNumber, address);
        init(valueMode);
    }

    private void init(ValueMode valueMode) {
        charset = StandardCharsets.US_ASCII;
        maxSize = 40;
        this.model = "Датчик pH"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.value = new DoubleValue(name, "Водородный показатель", valueMode, 3);
        values.add(this.value);
    }

    public double getValue(){
        return value.getValue();
    }

    public double getValue(boolean withUpdate) throws Exception{
        double doubleValue;
        if (withUpdate) {
            doubleValue = Double.parseDouble(readln("R", 900));
            value.setValue(doubleValue);
        } else
            doubleValue = value.getValue();
        return doubleValue;
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
