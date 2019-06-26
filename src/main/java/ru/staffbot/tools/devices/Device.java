package ru.staffbot.tools.devices;

import com.pi4j.io.gpio.Pin;
import ru.staffbot.tools.values.Value;

import java.util.ArrayList;

/**
 * <b>Устройство</b>
 * Базовый класс для всех драйверов работающих с устройствами<br>
 */
public abstract class Device{

    protected String name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
    protected String model; // Модель устройства - тип и модель датчика (например, "Сонар HC-SR04")
    protected String note; // Описание устройства (например, "Сонар для измерения уровня воды")

    /*
        Значения устройства
     */
    protected ArrayList<Value> values = new ArrayList();

    /**
     * <b>Сбросить</b> значение на значение по умолчанию ({@code defaultValue})<br>
     * Во всех классах из пакета {@link ru.staffbot.tools.levers} реализующих интеофейс {@code Lever}
     * данный метод автоматически реализуется в родительском классе {@link Value}<br>
     */
    protected void reset() {
        for(Value value: values)
            value.reset();
    }

    public String getName(){
        return name;
    }

    public String getNote(){
        return note;
    }

    public String getModel(){
        return model;
    }

    public ArrayList<DevicePin> getDevicePins(){
        ArrayList<DevicePin> devicePins = new ArrayList();
        for (Pin pin : Devices.pins.keySet())
            if (name.equals(Devices.pins.get(pin).device))
                devicePins.add(Devices.pins.get(pin));
        return devicePins;
    }

    public ArrayList<Pin> getPins(){
        ArrayList<Pin> pins = new ArrayList();
        for (Pin pin : Devices.pins.keySet())
            if (name.equals(Devices.pins.get(pin).device))
                pins.add(pin);
        return pins;
    }

    public ArrayList<Value> getValues(){
        return values;
    }

    public Value getValue(int index){
        return ((index > -1)&&(index < values.size())) ? values.get(index) : null;
    }

    protected long setValue(int index, long value){
        return ((index > -1)&&(index < values.size())) ? values.get(index).set(value) : 0;
    }

    public String getValueAsString(){
        return "";
    }


}
