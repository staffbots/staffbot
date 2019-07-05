package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.Pin;
import ru.staffbots.tools.values.Value;

import java.util.ArrayList;

/**
 * <b>Устройство</b>
 * Базовый класс для всех драйверов работающих с устройствами<br>
 */
public abstract class Device{

    // Уникальное имя устройства
    protected String name;
    public String getName(){
        return name;
    }

    // Модель устройства
    protected String model;
    public String getModel(){
        return model;
    }

    // Описание устройства
    protected String note;
    public String getNote(){
        return note;
    }

    /*
     *  Получаемые и/или передаваемые значения устройства
     */
    protected ArrayList<Value> values = new ArrayList();

    /**
     * <b>Сбросить</b> значение на значение по умолчанию<br>
     */
    protected void reset() {
        for(Value value: values)
            value.reset();
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

    @Override
    abstract public String toString();

    // Чтение данных,
    // Переопределяется для датчиков, считывающих данные
    public boolean dataRead(){
        return true;
    }

}
