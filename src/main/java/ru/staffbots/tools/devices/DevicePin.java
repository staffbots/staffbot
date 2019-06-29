package ru.staffbots.tools.devices;

/**
 * Пин устройства
 */
public class DevicePin {

    public String device; //имя устройства

    public String pinNote; //описание соответствующего пина на этом устройстве

    public DevicePin(String device){
        this.device = device;
        this.pinNote = "";
    }

    public DevicePin(String device, String pinNote){
        this.device = device;
        this.pinNote = pinNote;
    }

}
