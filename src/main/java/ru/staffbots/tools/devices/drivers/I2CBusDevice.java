package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.DevicePin;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;
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

    private I2CDevice device;

    private I2CBus bus;

    public int getBusNumber(){
        return bus.getBusNumber();
    }

    public int getAddress(){
        return device.getAddress();
    }


    public I2CBusDevice(String name, String note, int busNumber, int address) {

        this.model = "Шина I2C"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")

        for (int pinNumber = 0; pinNumber < 2; pinNumber++)
            Devices.putToPins(getPin(busNumber, pinNumber), new DevicePin(name, getPinNote(busNumber, pinNumber)));

        if (!Devices.USED) return;

        try {
            bus = I2CFactory.getInstance(busNumber);
            bus.getBusNumber();
            device = bus.getDevice(address);
        } catch (I2CFactory.UnsupportedBusNumberException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPinNote(int busNumber, int pinNumber){
        return ((pinNumber == 0) ? "SDA" : "SCL") + busNumber;
    }

    private Pin getPin(int busNumber, int pinNumber){
        // Для Rpi3
        switch (busNumber) {
            case 0: return (pinNumber == 0) ? RaspiPin.GPIO_30 : RaspiPin.GPIO_31;
            case 1: return (pinNumber == 0) ? RaspiPin.GPIO_08 : RaspiPin.GPIO_09;
        }
        return null;
    }

    @Override
    public String getModel(boolean byClassName) {
        if(!byClassName) return model;
        String className = (new Object(){}.getClass().getEnclosingClass().getSimpleName());
        return className.substring(0, className.length() - 6);
    }

    public void write(byte[] bytes) throws IOException{
        device.write(bytes);
    }

    public void write(String string) throws IOException{
        write(string.getBytes(charset));
    }

    public byte[] read() throws Exception{
        byte[] bytes = new byte[maxSize];
        device.read(bytes, 0, maxSize);
        int size = maxSize - 1;
        while (size > 1) {
            if (bytes[size] > 0) break;
            size--;
        }
        return Arrays.copyOfRange(bytes, 1, size + 1);

    }

    public String readln() throws Exception{
        return new String(read(), charset);
    }

    public String readln(String command, int delay) throws Exception{
        write(command);
        Thread.sleep(delay);
        return readln();
    }

}
