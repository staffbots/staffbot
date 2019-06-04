package ru.staffbot.utils.devices.hardware;

import com.pi4j.io.gpio.Pin;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.DevicePin;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.values.DoubleValue;

public class SensorDHT22Device extends Device {

    private DoubleValue temperature;
    private DoubleValue humidity;

    public SensorDHT22Device(String name, String note, Pin pin) {
        init(name, note, pin, true);
    }

    public SensorDHT22Device(String name, String note, Pin pin, Boolean dbStorage) {
        init(name, note, pin, dbStorage);
    }

    private void init(String name, String note, Pin pin, Boolean dbStorage) {

        this.model = "Датчик DHT22"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.temperature = new DoubleValue(name + "_temperature", "Температура", dbStorage);
        this.humidity = new DoubleValue(name + "_humidity", "Влажность", dbStorage);

        values.add(temperature);
        values.add(humidity);

        Devices.putToPins(pin, new DevicePin(name,"DATA"));

        if(!Devices.USED)return;
        //gpioPin = Devices.gpioController.provisionDigitalInputPin(pin);

    }


    public double getTemperature(boolean withMeassure) {
        if (withMeassure) {
            //измерение
        }
        return temperature.getValue();
    }

    public double getHumidity(boolean withMeassure) {
        if (withMeassure) {
            //измерение
        }
        return humidity.getValue();
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String getValueAsString(){
        return temperature.getNote() + " = " + temperature.getValue() + "\n" +
                humidity.getNote() + " = " + humidity.getValue() ;
    }
}
