package ru.staffbots.tools.devices.drivers.i2c;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import ru.staffbots.Staffbot;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * https://www.atlas-scientific.com/product_pages/kits/ph-kit.html
 */

public abstract class I2CBusDevice extends Device {

    protected Charset charset = StandardCharsets.US_ASCII;

    protected int maxSize = 40;

    protected I2CDevice device;

    private I2CBus bus;

    public int getBusNumber(){
        return bus.getBusNumber();
    }

    private int busAddress;

    public int getBusAddress(){
        return (device == null) ? busAddress : device.getAddress();
    }

    public I2CBusDevice(String name, String note, int busNumber, int busAddress) {

        this.model = "I2C bus"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.busAddress = busAddress;

        for (int pinNumber = 0; pinNumber < 2; pinNumber++)
            putPin(getPin(busNumber, pinNumber),getPinNote(pinNumber));

        if (!Devices.USED) return;

        try {

            bus = I2CFactory.getInstance(busNumber);
            bus.getBusNumber();
            device = bus.getDevice(busAddress);
        } catch (I2CFactory.UnsupportedBusNumberException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPinNote(int pinNumber){
        return ((pinNumber == 0) ? "SDA" : "SCL");
    }

    private Pin getPin(int busNumber, int pinNumber){
        switch (Staffbot.boardType) {
            case RaspberryPi_B_Plus:
            case RaspberryPi_2B:
            case RaspberryPi_3B:
            case RaspberryPi_3B_Plus:
                switch (busNumber) {
                    case 0: return (pinNumber == 0) ? RaspiPin.GPIO_30 : RaspiPin.GPIO_31;
                    case 1: return (pinNumber == 0) ? RaspiPin.GPIO_08 : RaspiPin.GPIO_09;
                }
        }
        return null;
    }

    public void write(byte[] data) throws IOException{
        device.write(data);
    }

    public void write(byte data) throws IOException{
        device.write(data);
    }

    public void write(String string) throws IOException{
        write(string.getBytes(charset));
    }

    public byte[] read() throws Exception{
        byte[] bytes = new byte[maxSize];
        device.read(bytes, 0, maxSize);
        return bytes;
    }

    public String readln() throws Exception{
        byte[] bytes = read();
        int size = maxSize - 1;
        while (size > 1) {
            if (bytes[size] > 0) break;
            size--;
        }
        return new String(Arrays.copyOfRange(bytes, 1, size + 1), charset);
    }

    public String readln(String command, int delay) throws Exception{
        write(command);
        Thread.sleep(delay);
        return readln();
    }

    static public I2CBusDevice convertDevice(Device device){
        return (device instanceof I2CBusDevice) ? (I2CBusDevice) device : null;
    }
}
