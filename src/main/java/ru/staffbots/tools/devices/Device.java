package ru.staffbots.tools.devices;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import ru.staffbots.Staffbot;
import ru.staffbots.tools.values.Value;
import ru.staffbots.webserver.servlets.*;

import java.util.*;

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
        List<Pin> boardPins = Arrays.asList(RaspiPin.allPins(Staffbot.getBoardType()));
        if (!boardPins.contains(pin))
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
    public String getNote(String languageCode){
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

    abstract public boolean initPins();

    // Чтение данных,
    // Переопределяется для датчиков, считывающих данные
    public boolean dataRead(){
        return true;
    }

    //
    public String getLink(){
        return Staffbot.getProjectWebsite() + "/technology/devices/" + getModel(true);
    }

    public void dbInit(){
        for (Value value : getValues())
            value.dbInit();
    }

}
