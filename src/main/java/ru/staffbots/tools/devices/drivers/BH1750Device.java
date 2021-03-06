package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.gpio.PinState;
import ru.staffbots.Staffbot;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.SystemInformation;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.i2c.I2CBusDevice;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

public class BH1750Device extends I2CBusDevice {

    static final int lowAddress = 0x23;

    static final int highAddress = 0x5C;

    private DoubleValue lightLevel;

    public BH1750Device(String name, String note, int busNumber) {
        super(name, note, busNumber, lowAddress);
        init(ValueMode.STORABLE);
    }

    public BH1750Device(String name, String note, ValueMode valueMode, int busNumber) {
        super(name, note, busNumber, lowAddress);
        init(valueMode);
    }

    public BH1750Device(String name, String note, int busNumber, PinState addressState) {
        super(name, note, busNumber, (addressState == PinState.LOW) ? lowAddress : highAddress);
        init(ValueMode.STORABLE);
    }

    public BH1750Device(String name, String note, ValueMode valueMode, int busNumber, PinState addressState) {
        super(name, note, busNumber, (addressState == PinState.LOW) ? lowAddress : highAddress);
        init(valueMode);
    }

    private void init(ValueMode valueMode) {
        charset = StandardCharsets.US_ASCII;
        model = "Ambient Light Sensor";
        lightLevel = new DoubleValue(name, "Light level, lux", valueMode, 2, 0.11 , 100000.00 );
        values.add(lightLevel);
        maxSize = 2;
    }

    @Override
    public boolean initPins() {
        if (!SystemInformation.isRaspbian) return false;
        return true;
    }

    public double getLightIntensity() {
        return getLightIntensity(true);
    }

    public double getLightIntensity(boolean withUpdate){
        double value = lightLevel.getValue();
        if (withUpdate)
        try {
            Integer reolution = (getBusAddress() == lowAddress) ? 0x10 : 0x23;
            write(reolution.byteValue());
            Thread.sleep(200);
            byte[] buff = read();
            if(buff.length == 2){
                value = ((buff[0] << 8) | buff[1]) / 1.2;
                lightLevel.setValue(value);
            }
            else
                Journal.add(NoteType.ERROR, "any_message", "Мало намерено");
        } catch (Exception e) {
            Journal.add(NoteType.ERROR, "any_message", "С освещением беда!");
        }
        return value;
    }


    @Override
    public String toString() {
        return lightLevel.toString();
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}
