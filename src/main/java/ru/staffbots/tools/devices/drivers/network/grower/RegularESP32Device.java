package ru.staffbots.tools.devices.drivers.network.grower;

import ru.staffbots.tools.devices.drivers.network.AddressSettings;
import ru.staffbots.tools.devices.drivers.network.NetworkDevice;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;

public class RegularESP32Device extends NetworkDevice {

    private DoubleValue lightLevel;
    private BooleanValue valveRelay;
    private DoubleValue airHumidity;
    private DoubleValue airTemperature;
    private DoubleValue soilMoisture;

    public RegularESP32Device(AddressSettings addressSettings, String name, String note) {
        super(addressSettings, name, note);
        init(ValueMode.STORABLE);
    }

    public RegularESP32Device(AddressSettings addressSettings, String name, String note, ValueMode valueMode) {
        super(addressSettings, name, note);
        init(valueMode);
    }

    private void init(ValueMode valueMode) {
        this.model = "ESP32";
        valveRelay = new BooleanValue(name + "_valveRelay", "Valve relay", valueMode, false);
        lightLevel = new DoubleValue(name + "_lightLevel", "Light level, lux", valueMode, 2, 0.1 , 100000d );
        airTemperature = new DoubleValue(name + "_airTemperature", "Air temperature, °С", valueMode, 2, 0d, 50d );;
        airHumidity = new DoubleValue(name + "_airHumidity", "Air humidity, %", valueMode, 2, 20d, 90d );
        soilMoisture = new DoubleValue(name + "_soilMoisture", "Soil moisture, %", valueMode, 2 );
        values.add(valveRelay);
        values.add(lightLevel);
        values.add(airTemperature);
        values.add(airHumidity);
        values.add(soilMoisture);
    }

    @Override
    public boolean initPins() {
        return true;
    }

    @Override
    public void initValues() {
        super.initValues();
        setValveRelay(valveRelay.getValue());
        getLightLevel();
    }

    public void setValveRelay(boolean value){

        if (post(valveRelay.getName() + "=" + (value ? "on" : "off")) != null)
            valveRelay.setValue(value);
    }

    public boolean getValveRelay(){
        return valveRelay.getValue();
    }

    public double getLightLevel() {
        return getLightLevel(true);
    }

    public double getLightLevel(boolean withUpdate){
        return getValue(lightLevel, true);
    }

    public double getAirTemperature() {
        return getAirTemperature(true);
    }

    public double getAirTemperature(boolean withUpdate) {
        return getValue(airTemperature, true);
    }

    public double getAirHumidity() {
        return getAirHumidity(true);
    }

    public double getAirHumidity(boolean withUpdate) {
        return getValue(airHumidity, true);
    }

    public double getSoilMoisture() {
        return getSoilMoisture(true);
    }

    public double getSoilMoisture(boolean withUpdate) {
        return getValue(soilMoisture, true);
    }

    private double getValue(DoubleValue variable, boolean withUpdate){
        if (!withUpdate) return variable.getValue();
        double value = getAsDouble(variable.getName(), variable.getValue());
        variable.setValue(value);
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
