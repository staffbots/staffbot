package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;


/**
 * Ультразвуковой дальномер HC-SR04
 */
public class SonarHCSR04Device extends Device {

    /**
     * Скорость звука, м/с
     */
    private final static double SOUND_SPEED = 340.29d;

    /**
     * Продолжительность сигнала, микросекунд
     */
    private final static int TRIG_DURATION = 10;

    private final static int TIMEOUT = 2100;

    // Точность - количество знаков после запятой
    private final static int ACCURACY = 2;


    private GpioPinDigitalOutput gpioPinTRIG;

    private GpioPinDigitalInput gpioPinECHO;

    private DoubleValue value;

    public SonarHCSR04Device(String name, String note, Pin pinTRIG, Pin pinECHO) {
        init(name, note, ValueMode.STORABLE, pinTRIG, pinECHO);
    }

    public SonarHCSR04Device(String name, String note, ValueMode valueMode, Pin pinTRIG, Pin pinECHO) {
        init(name, note, valueMode, pinTRIG, pinECHO);
    }

    private void init(String name, String note, ValueMode valueMode, Pin pinTRIG, Pin pinECHO) {

        this.model = "Сонар HC-SR04"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.value = new DoubleValue(name, "Дистанция, см", valueMode, ACCURACY);

        values.add(this.value);
        putPin(pinTRIG,"TRIG");
        putPin(pinECHO,"ECHO");

//        if (!Devices.putDevice(this)) return;

        if (!Devices.USED) return;
        gpioPinTRIG = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0));
        gpioPinECHO = Devices.gpioController.provisionDigitalInputPin(getPins().get(1));
    }

    @Override
    public boolean dataRead(){
        try {
            triggerSensor();
            waitForSignal();
            long duration = measureSignal(); //микроскекунд
            value.setValue(duration * SOUND_SPEED / ( 2 * 10000 ));
        } catch (Exception exception) {
            Journal.add(note + " -  reading error: " + exception.getMessage());
            return false;
        }
        return true;
    }

    /**
     *
     * @return Дистанция в сантиметрах
     */
    public double getDistance() {
        dataRead();
        return value.getValue(); //см
    }

    /**
     * Посылает ультразвук продолжительностью TRIG_DURATION микросекунд
     */
    private void triggerSensor() throws Exception {
        gpioPinTRIG.low();
        Thread.sleep(500);
        gpioPinTRIG.high();
        Thread.sleep(0, TRIG_DURATION * 1000);
        gpioPinTRIG.low();
    }

    /**
     * Ожидание начала обратного сигнала
     * @throws Exception если сигнал не начался во время
     */
    private void waitForSignal() throws Exception {
        int countdown = TIMEOUT;
        while (gpioPinECHO.isLow() && countdown > 0)
            countdown--;
        if(countdown <= 0)
            throw new Exception( "Время ожидания начала обратного сигнала вышло" );
    }

    /**
     * @return Продолжительность обратного сигнала в миикросекундах
     * @throws Exception если сигнал не закончился во время
     */
    private long measureSignal() throws Exception {
        int countdown = TIMEOUT;
        long start = System.nanoTime();
        while(gpioPinECHO.isHigh() && countdown > 0)
            countdown--;
        long end = System.nanoTime();
        if( countdown <= 0 ) throw
                new Exception( "Время ожидания окончания обратного сигнала вышло");
        return (long)Math.ceil( ( end - start ) / 1000.0 );
    }

    /**
     * <b>Получить значение для отображения</b><br>
     *
     * @return Значение для отображения
     */
    @Override
    public String toString() {
        return this.value.toString();
        //return this.value.getValue() ? "Включен" : "Выключен";
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}
