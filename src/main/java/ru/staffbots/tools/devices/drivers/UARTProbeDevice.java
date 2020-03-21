package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.*;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// Класс не доделан в части декодирования строки, приходящей от устройства
public abstract class UARTProbeDevice extends Device {

    /**
     * Время последнего измерения
     */
    private long lastMeasureTime = 0;

    public Charset charset = StandardCharsets.US_ASCII;

    private Serial serial = SerialFactory.createInstance();

    private DoubleValue value;

    public UARTProbeDevice(String name, String note) {
        init(name, note, ValueMode.STORABLE, RaspiPin.GPIO_15, RaspiPin.GPIO_16);
    }

    public UARTProbeDevice(String name, String note, ValueMode valueMode, Pin txPin, Pin rxPin) {
        init(name, note, valueMode, txPin, rxPin);
    }

    private void init(String name, String note, ValueMode valueMode, Pin txPin, Pin rxPin) {
        this.model = "Датчик"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.value = new DoubleValue(name, "Водородный показатель", valueMode, 3);

        values.add(this.value);

        putPin(txPin, "Tx");
        putPin(rxPin, "Rx");

//        if (!Devices.putDevice(this)) return;

        if (!Devices.isRaspbian) return;

        serial.addListener(new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                try {
                    //Cтрока, приходящей от устройства
                    //System.out.println(event.getHexByteString("0x", " ", ""));
                    System.out.println(event.getAsciiString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        SerialConfig config = new SerialConfig();
        try {
            config.device(SerialPort.getDefaultPort())
                    .baud(Baud._9600)
                    .dataBits(DataBits._8)
                    .parity(Parity.NONE)
                    .stopBits(StopBits._2)
                    .flowControl(FlowControl.NONE);
            serial.open(config);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void write(String string) throws IOException{
        write(string.getBytes(charset));
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public void write(byte[] bytes) throws IOException{
        int n = bytes.length;
        byte[] out = new byte[n + 1];
        for (int i = 0; i < n; i++)
            out[i] = bytes[i];
        out[n] = 0x0D;
        serial.write(out);
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}
