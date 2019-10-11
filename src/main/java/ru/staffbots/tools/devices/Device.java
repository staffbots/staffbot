package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.Pin;
import ru.staffbots.Pattern;
import ru.staffbots.tools.values.Value;
import ru.staffbots.webserver.servlets.*;

import java.net.URL;
import java.util.ArrayList;

/**
 * <b>Устройство</b>
 * Базовый класс для всех драйверов работающих с устройствами<br>
 */
public abstract class Device{

    // Уникальное имя устройства, значение задаётся при описании объекта дочернего класса
    // (конкретного устройства)
    protected String name;

    // Возвращает уникальное имя устройства, используется в интерфейсе
    public String getName(){
        return name;
    }


    // Модель устройства, значение присваивается во время инициализации дочернего класса
    // (драйвера конкретного устройства)
    protected String model;

    /**
     * Возвращает модель устройства ({@link Device#model}),
     * <br>используется при формировании веб-интерфейса
     * в классах {@link ControlServlet} и {@link AboutServlet}
     * @see Device#getModel(boolean byClassName)
     */
    public String getModel(){
        return model;
    }

    // При значении параметра byClassName
    // true - работает как getModel();
    // false - возвращает часть имени класса-дравера, используется в URL
    abstract public String getModel(boolean byClassName);

    // Описание устройства, значение задаётся при описании объекта дочернего класса
    // (конкретного устройства)
    protected String note;

    // Возвращает описание устройства, используется в интерфейсе
    public String getNote(){
        return note;
    }

    /*****************************************************
     *  Получаемые и/или передаваемые значения устройства
     ****************************************************/
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

    //
    public URL getURL(){
        try {
             //return new URL("http://" + Pattern.website);
            return new URL(Pattern.projectWebsite + "/technology/devices/" + getModel(true));
            //return new URL(Pattern.projectWebsite + "/technology/devices/" + urlName);
        } catch (Exception exception) {
            return null;
        }
    }


}
