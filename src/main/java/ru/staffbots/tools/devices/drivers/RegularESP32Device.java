package ru.staffbots.tools.devices.drivers;

import ru.staffbots.tools.devices.drivers.general.NetworkDevice;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;


public class RegularESP32Device extends NetworkDevice {

    private DoubleValue lightIntensity;

    public RegularESP32Device(String name, String note) {
        super(name, note);
        init(ValueMode.STORABLE);
    }

    public RegularESP32Device(String name, String note, ValueMode valueMode) {
        super(name, note);
        init(valueMode);
    }

    private void init(ValueMode valueMode) {
        this.model = "ESP-WROOM-32";
        lightIntensity = new DoubleValue(name + "_intensity", "Light intensity, lux", valueMode, 2, 0.11 , 100000.00 );
        values.add(lightIntensity);
    }

    public double getLightIntensity() {
        return getLightIntensity(true);
    }

    public double getLightIntensity(boolean withUpdate){
        if (!withUpdate) return lightIntensity.getValue();
        double value = lightIntensity.getValue();
        String stringValue = get(lightIntensity.getName());
        if ((stringValue == null) || stringValue.isEmpty())
            return value;
        try {
            value = Double.parseDouble(stringValue);
        } catch (NumberFormatException e) {
            value = lightIntensity.getValue();
        }
        lightIntensity.setValue(value);
        return value;
    }


    @Override
    public String toString() {
        return lightIntensity.toString();
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}
