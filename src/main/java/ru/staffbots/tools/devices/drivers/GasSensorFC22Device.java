package ru.staffbots.tools.devices.drivers;

import ru.staffbots.tools.devices.Device;

public class GasSensorFC22Device extends Device {

    @Override
    public String getModel(boolean byClassName) {
        if(!byClassName) return model;
        String className = (new Object(){}.getClass().getEnclosingClass().getSimpleName());
        return className.substring(0, className.length() - 6);
    }

    @Override
    public String toString() {
        return null;
    }

}
