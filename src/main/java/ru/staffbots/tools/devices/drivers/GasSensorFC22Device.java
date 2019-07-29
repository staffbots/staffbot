package ru.staffbots.tools.devices.drivers;

import ru.staffbots.tools.devices.Device;

public class GasSensorFC22Device extends Device {

    @Override
    public String getModelName() {
        String className = (new Object(){}.getClass().getEnclosingClass().getSimpleName());
        return className.substring(0, className.length() - 6);
    }

    @Override
    public String toString() {
        return null;
    }

}
