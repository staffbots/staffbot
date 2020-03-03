package ru.staffbots.tools.devices.drivers.network.grower;

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
        this.model = "ESP32";
        valveRelay = new BooleanValue(name + "_valveRelay", "Valve relay", valueMode, false);
        lightLevel = new DoubleValue(name + "_lightLevel", "Light level, lux", valueMode, 2, 0.11 , 100000.00 );
        values.add(valveRelay);
        values.add(lightLevel);
    }

    @Override
    public void initValue() {
        super.initValue();
        setValveRelay(valveRelay.getValue());
        getLightLevel();
    }

    public void setValveRelay(boolean value){
        if (post(valveRelay.getName() + "=" + (value ? "on" : "off")) != null)
            valveRelay.setValue(value);
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

    @Override
    public String getLink(){
        return getInoResourceLink(getClassName());
    }

}
