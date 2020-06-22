package ru.staffbots.tools.devices.drivers.network.grower;

import ru.staffbots.tools.devices.drivers.network.AddressSettings;
import ru.staffbots.tools.devices.drivers.network.NetworkDevice;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;

public class MainESP32Device extends NetworkDevice {

    private BooleanValue ecPumpRelay;
    private BooleanValue phUpPumpRelay;
    private BooleanValue phDownPumpRelay;
    private DoubleValue aquaTemperature;
    private DoubleValue phProbe;
    private DoubleValue ecProbe;

    public MainESP32Device(AddressSettings addressSettings, String name, String note) {
        super(addressSettings, name, note);
        init(ValueMode.STORABLE);
    }

    public MainESP32Device(AddressSettings addressSettings, String name, String note, ValueMode valueMode) {
        super(addressSettings, name, note);
        init(valueMode);
    }

    private void init(ValueMode valueMode) {
        this.model = "ESP32";
        ecPumpRelay = new BooleanValue(name + "_ecPumpRelay", "EC pump relay", valueMode, false);
        phUpPumpRelay = new BooleanValue(name + "_phUpPumpRelay", "Ph+ pump relay", valueMode, false);
        phDownPumpRelay = new BooleanValue(name + "_phDownPumpRelay", "Ph- pump relay", valueMode, false);
        aquaTemperature = new DoubleValue(name + "_aquaTemperature", "Aqua temperature, °С", valueMode, 1, -55d, 125d );
        phProbe = new DoubleValue(name + "_phProbe", "pH probe", valueMode, 3, 0d, 14d );
        ecProbe = new DoubleValue(name + "_ecProbe", "EC probe (K=1), ms/cm", valueMode, 3, 0d, 20d );
        values.clear();
        values.add(phProbe);
        values.add(ecProbe);
        values.add(aquaTemperature);
       // values.add(ecPumpRelay);
       // values.add(phUpPumpRelay);
       // values.add(phDownPumpRelay);
    }

    @Override
    public boolean initPins() {
        return true;
    }

    @Override
    public void dbInit() {
        super.dbInit();
        getPhProbe();
        getEcProbe();
        getAquaTemperature();
    }

    public void setEcPumpRelay(boolean value) {
        setBooleanValue(ecPumpRelay, value);
    }

    public boolean getEcPumpRelay() {
        return getBooleanValue(ecPumpRelay);
    }

    public void setPhUpPumpRelay(boolean value) {
        setBooleanValue(phUpPumpRelay, value);
    }

    public boolean getPhUpPumpRelay() {
        return getBooleanValue(phUpPumpRelay);
    }

    public void setPhDownPumpRelay(boolean value) {
        setBooleanValue(phDownPumpRelay, value);
    }

    public boolean getPhDownPumpRelay() {
        return getBooleanValue(phDownPumpRelay);
    }

    public double getAquaTemperature() {
        return getAquaTemperature(true);
    }

    public double getAquaTemperature(boolean withUpdate) {
        return getDoubleValue(aquaTemperature, true);
    }

    public double getPhProbe() {
        return getPhProbe(true);
    }

    public double getPhProbe(boolean withUpdate) {
        return getDoubleValue(phProbe, true);
    }

    public double getEcProbe() {
        return getEcProbe(true);
    }

    public double getEcProbe(boolean withUpdate) {
        return getDoubleValue(ecProbe, true);
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
