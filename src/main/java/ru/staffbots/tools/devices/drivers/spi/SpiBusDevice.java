package ru.staffbots.tools.devices.drivers.spi;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import ru.staffbots.tools.devices.Device;

import java.io.IOException;

public abstract class SpiBusDevice extends Device {

    private SpiChannel channel = null;

    private SpiDevice device;

    public int getBusChannel(){
        return (channel == null) ? -1 : channel.getChannel();
    }

    public SpiBusDevice(SpiChannel channel){
        this.channel = channel;
        try {
            device = SpiFactory.getInstance(channel,
                     SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                     SpiDevice.DEFAULT_SPI_MODE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public SpiBusDevice convertDevice(Device device){
        return (device instanceof SpiBusDevice) ? (SpiBusDevice) device : null;
    }

}
