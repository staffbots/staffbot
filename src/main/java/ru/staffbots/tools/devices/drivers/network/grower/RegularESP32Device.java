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
    public void dbInit() {
        super.dbInit();
        setValveRelay(valveRelay.getValue());
        getLightLevel();
    }

    public void setValveRelay(boolean value){
        setBooleanValue(valveRelay, value);
    }

    public boolean getValveRelay(){
        return getBooleanValue(valveRelay);
    }

    public double getLightLevel() {
        return getLightLevel(true);
    }

    public double getLightLevel(boolean withUpdate){
        return getDoubleValue(lightLevel, true);
    }

    public double getAirTemperature() {
        return getAirTemperature(true);
    }

    public double getAirTemperature(boolean withUpdate) {
        return getDoubleValue(airTemperature, true);
    }

    public double getAirHumidity() {
        return getAirHumidity(true);
    }

    public double getAirHumidity(boolean withUpdate) {
        return getDoubleValue(airHumidity, true);
    }

    public double getSoilMoisture() {
        return getSoilMoisture(true);
    }

    public double getSoilMoisture(boolean withUpdate) {
        return getDoubleValue(soilMoisture, true);
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
