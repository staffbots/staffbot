package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import ru.staffbots.Staffbot;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.devices.drivers.general.I2CBusDevice;
import ru.staffbots.tools.devices.drivers.general.SpiBusDevice;
import ru.staffbots.tools.values.Value;
import ru.staffbots.webserver.servlets.*;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>Устройство</b>
 * Базовый класс для всех драйверов работающих с устройствами<br>
 */
public abstract class Device{

    public boolean overlap = false;


    /**
     * Массив пинов с привязкой к пину на этом устройстве
     */
    private Map<Pin, String> pins = new HashMap(0);

    public ArrayList<Pin> getPins(){
        ArrayList<Pin> result = new ArrayList(0);
        for (Pin pin: pins.keySet())
            result.add(pin);
        return result;
    }

    public boolean putPin(Pin pin, String name){
        Pin[] boardPins = RaspiPin.allPins(Staffbot.boardType);
        boolean pinPosible = false;
        for(Pin boardPin: boardPins)
            pinPosible = pinPosible || (boardPin == pin);
        if (!pinPosible)
            return false;
        if (pins.containsKey(pin)){
            overlap = true;
            return false;
        } else {
            pins.put(pin, name);
            return true;
        }
    }

    public String getPinName(Pin pin){
        return pins.containsKey(pin) ? pins.get(pin) : "";
    }

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
    public String getModel(boolean byClassName){
        if(!byClassName) return model;
        String className = getClassName();
        return className.substring(0, className.length() - 6);
    }

    abstract public String getClassName();

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
            return new URL(Staffbot.projectWebsite + "/technology/devices/" + getModel(true));
            //return new URL(Pattern.projectWebsite + "/technology/devices/" + urlName);
        } catch (Exception exception) {
            return null;
        }
    }

    public int getI2CBusAddress() {
       return (this instanceof I2CBusDevice) ? ((I2CBusDevice) this).getBusAddress() : -1;
    }

    public int getSpiBusChannel() {
        return (this instanceof SpiBusDevice) ? ((SpiBusDevice) this).getBusChannel() : -1;
    }

}
