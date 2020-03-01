package ru.staffbots.tools.devices.drivers.network.grower;

import com.pi4j.component.relay.Relay;
import ru.staffbots.tools.devices.drivers.network.NetworkDevice;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;


public class RegularESP32Device extends NetworkDevice {

    private DoubleValue lightLevel;
    private BooleanValue valveRelay;

    public RegularESP32Device(String address, String name, String note) {
        super(address, name, note);
        init(ValueMode.STORABLE);
    }

    public RegularESP32Device(String address, String name, String note, ValueMode valueMode) {
        super(address, name, note);
        init(valueMode);
    }

    private void init(ValueMode valueMode) {
        this.model = "ESP-WROOM-32";
        lightLevel = new DoubleValue(name + "_lightLevel", "Light level, lux", valueMode, 2, 0.11 , 100000.00 );
        valveRelay = new BooleanValue(name + "_valveRelay", "Valve relay", valueMode, true);
        values.add(lightLevel);
        values.add(valveRelay);
    }

    public double getLightLevel() {
        return getLightLevel(true);
    }

    public double getLightLevel(boolean withUpdate){
        if (!withUpdate) return lightLevel.getValue();
        double value = getAsDouble(lightLevel.getName(), lightLevel.getValue());
        lightLevel.setValue(value);
        return value;
    }


    @Override
    public String toString() {
        return model + ": " + name;
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}
