package ru.staffbot.utils.devices.hardware;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.DevicePin;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.values.DoubleValue;


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

    private GpioPinDigitalOutput gpioPinTRIG;
    private GpioPinDigitalInput gpioPinECHO;
    private DoubleValue value;

    public SonarHCSR04Device(String name, String note, Pin pinTRIG, Pin pinECHO) {
        init(name, note, true, pinTRIG, pinECHO);
    }

    public SonarHCSR04Device(String name, String note, boolean dbStorage, Pin pinTRIG, Pin pinECHO) {
        init(name, note, dbStorage, pinTRIG, pinECHO);
    }

    private void init(String name, String note, boolean dbStorage, Pin pinTRIG, Pin pinECHO) {

        this.model = "Сонар HC-SR04"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")
        this.value = new DoubleValue(name, "Дистанция, см", dbStorage);

        values.add(this.value);
        Devices.putToPins(pinTRIG, new DevicePin(name, "TRIG"));
        Devices.putToPins(pinECHO, new DevicePin(name, "ECHO"));

        if (!Devices.USED) return;
        gpioPinTRIG = Devices.gpioController.provisionDigitalOutputPin(getPins().get(0));
        gpioPinECHO = Devices.gpioController.provisionDigitalInputPin(getPins().get(1));
    }

    /**
     *
     * @return Дистанция в сантиметрах
     * @throws Exception
     */
    public double getDistance() throws Exception {
        triggerSensor();
        waitForSignal();
        long duration = measureSignal(); //микроскекунд
        value.setValue(duration * SOUND_SPEED / ( 2 * 10000 ));
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
    public String getValueAsString() {
        return this.value.getValueAsString();
        //return this.value.getValue() ? "Включен" : "Выключен";
    }

}
