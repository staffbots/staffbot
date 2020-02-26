package ru.staffbots.tools.devices.drivers;

import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.Gpio;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.ValueMode;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.concurrent.*;

public class SensorDHT22Device extends Device {

    // Минимальная задержка между измерениями, миллисекуд
    private final static long MIN_DELAY = 2000;
    // Точность - количество знаков после запятой
    private final static int ACCURACY = 2;
    private DoubleValue temperature;
    private DoubleValue humidity;

    public SensorDHT22Device(String name, String note, Pin pin) {
        init(name, note, ValueMode.STORABLE, pin);
    }

    public SensorDHT22Device(String name, String note, ValueMode valueMode, Pin pin) {
        init(name, note, valueMode, pin);
    }

    private void init(String name, String note, ValueMode valueMode, Pin pin) {

        this.model = "Датчик DHT22"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")
        this.note = note; // Описание устройства (например, "Сонар для измерения уровня воды")
        this.name = name; // Уникальное имя устройства, используется для именования таблиц в БД (например, "WaterSonar")

        this.temperature = new DoubleValue(name + "_temperature", "Температура, C", valueMode, ACCURACY);
        this.humidity = new DoubleValue(name + "_humidity", "Влажность, %", valueMode, ACCURACY);

        values.add(this.temperature);
        values.add(this.humidity);

        putPin(pin, "DATA");

//        if (!Devices.putDevice(this)) return;

        pinNumber = pin.getAddress();

        if (!Devices.USED) return;

        if (Gpio.wiringPiSetup() == -1)
            Journal.addAnyNote(getName() + ": GPIO wiringPiSetup Failed!");
        //gpioPin = Devices.gpioController.provisionDigitalInputPin(pin);

    }

    public double getTemperature() {
        return getTemperature(true);
    }

    public double getHumidity() {
        return getHumidity(true);
    }
    public double getTemperature(boolean withMeassure) {
        if (withMeassure)
            dataRead();
        return temperature.getValue();
    }

    public double getHumidity(boolean withMeassure) {
        if (withMeassure)
            dataRead();
        return humidity.getValue();
    }

    /**
     * <b>Получить значение для отображения</b><br>
     * @return Значение для отображения
     */
    @Override
    public String toString(){
        return temperature.getNote() + " = " + temperature.getValue() + "\n" +
                humidity.getNote() + " = " + humidity.getValue() ;
    }

    /**
     * Time in nanoseconds to separate ZERO and ONE signals.
     */
    private static final int LONGEST_ZERO = 50000;

    /**
     * 40 bit Data from sensor
     */
    private byte[] data = null;

    /**
     * Last read attempt
     */
    private Long lastRead = null;

    /**
     * PI4J Pin number.
     */
    private int pinNumber;

    /**
     * Communicate with sensor to get new reading data.
     *
     * @throws Exception if failed to successfully read data.
     */
    private void getData() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ReadSensorFuture readSensor = new ReadSensorFuture();
        Future<byte[]> future = executor.submit(readSensor);
        // Reset data
        data = new byte[5];
        try {
            data = future.get(3, TimeUnit.SECONDS);
            readSensor.close();
        } catch (TimeoutException e) {
            readSensor.close();
            future.cancel(true);
            executor.shutdown();
            System.out.println(note + " - Ошибка в методе getData()");
            throw e;
        }
        readSensor.close();
        executor.shutdown();
    }

    /**
     * Make a new sensor reading.
     */
    @Override
    public boolean dataRead(){

        if (!checkLastReadDelay())
            return false;

        lastRead = System.currentTimeMillis();
        try {
            getData();
            checkParity();
        } catch (Exception exception) {
            Journal.addAnyNote(note + " - reading error: " + exception.getMessage());
            return false;
        }
        humidity.setValue(getReadingValueFromBytes(data[0], data[1]));

        temperature.setValue(getReadingValueFromBytes(data[2], data[3]));
        //System.out.println("Измерение T = " + temperature.getValue());
        lastRead = System.currentTimeMillis();
        return true;

    }

    private boolean checkLastReadDelay(){
        if (Objects.nonNull(lastRead)) {
            if (System.currentTimeMillis() - lastRead < MIN_DELAY) {
                return false;
            }
        }
        return true;
    }

    private double getReadingValueFromBytes(final byte hi, final byte low) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(hi);
        bb.put(low);
        short shortVal = bb.getShort(0);
        return new Double(shortVal) / 10;
    }

    private void checkParity() throws ParityChheckException {
        if (!(data[4] == (data[0] + data[1] + data[2] + data[3] & 0xFF))) {
            //throw new ParityChheckException();
            System.out.println(note + " - Ошибка в методе checkParity()");
        }
    }

    /**
     * Callable Future for reading sensor.  Allows timeout if it gets stuck.
     */
    private class ReadSensorFuture implements Callable<byte[]>, Closeable {

        private boolean keepRunning = true;

        public ReadSensorFuture() {
            Gpio.pinMode(pinNumber, Gpio.OUTPUT);
            Gpio.digitalWrite(pinNumber, Gpio.HIGH);
        }

        @Override
        public byte[] call() throws Exception {

            // do expensive (slow) stuff before we start.
            byte[] data = new byte[5];
            long startTime = System.nanoTime();

            sendStartSignal();
            waitForResponseSignal();
            for (int i = 0; i < 40; i++) {
                while (keepRunning && Gpio.digitalRead(pinNumber) == Gpio.LOW) {
                }
                startTime = System.nanoTime();
                while (keepRunning && Gpio.digitalRead(pinNumber) == Gpio.HIGH) {
                }
                long timeHight = System.nanoTime() - startTime;
                data[i / 8] <<= 1;
                if ( timeHight > LONGEST_ZERO) {
                    data[i / 8] |= 1;
                }
            }
            return data;
        }

        private void sendStartSignal() {
            // Send start signal.
            Gpio.pinMode(pinNumber, Gpio.OUTPUT);
            Gpio.digitalWrite(pinNumber, Gpio.LOW);
            Gpio.delay(1);
            Gpio.digitalWrite(pinNumber, Gpio.HIGH);
        }

        /**
         * AM2302 will pull low 80us as response signal, then
         * AM2302 pulls up 80us for preparation to send data.
         */
        private void waitForResponseSignal() {
            Gpio.pinMode(pinNumber, Gpio.INPUT);
            while (keepRunning && Gpio.digitalRead(pinNumber) == Gpio.HIGH) {
            }
            while (keepRunning && Gpio.digitalRead(pinNumber) == Gpio.LOW) {
            }
            while (keepRunning && Gpio.digitalRead(pinNumber) == Gpio.HIGH) {
            }
        }

        @Override
        public void close() throws IOException {
            keepRunning = false;

            // Set pin high for end of transmission.
            Gpio.pinMode(pinNumber, Gpio.OUTPUT);
            Gpio.digitalWrite(pinNumber, Gpio.HIGH);
        }
    }

    private class ParityChheckException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}
