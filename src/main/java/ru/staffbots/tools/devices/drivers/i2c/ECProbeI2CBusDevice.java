package ru.staffbots.tools.devices.drivers.i2c;

import ru.staffbots.tools.devices.drivers.i2c.I2CBusDevice;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.LongValue;
import ru.staffbots.tools.values.ValueMode;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

public class ECProbeI2CBusDevice extends I2CBusDevice {

    private DoubleValue conductivity;// µS/cm
    private LongValue totalDissolvedSolids;//ppm
    private DoubleValue salinity;//PSU (ppt) 0.00 – 42.00
    private DoubleValue specificGravity;//1.00 – 1.300

    public ECProbeI2CBusDevice(String name, String note, int busNumber, int address) {
        super(name, note, busNumber, address);
        init(ValueMode.STORABLE);
    }

    public ECProbeI2CBusDevice(String name, String note, ValueMode valueMode, int busNumber, int address) {
        super(name, note, busNumber, address);
        init(valueMode);
    }

    private void init(ValueMode valueMode) {
        charset = StandardCharsets.US_ASCII;
        maxSize = 40;
        model = "Датчик EC"; // Тип устройства - тип и модель датчика (например, "Сонар HC-SR04")

        //EC,TDS,SAL,SG
        conductivity = new DoubleValue(name + "_ec", "Проводимость (EC), µS/cm", valueMode, 3);
        totalDissolvedSolids = new LongValue(name + "_tds",
                "Общее количество растворенных твердых веществ (TDS), ppm", valueMode);
        salinity = new DoubleValue(name + "_sal", "Соленость (SAL), PSU (ppt)", valueMode, 2);
        specificGravity = new DoubleValue(name + "_sg", "Удельный вес, только для морской воды (SG)", valueMode, 3);

        values.add(conductivity);// µS/cm
        values.add(totalDissolvedSolids);//ppm
        values.add(salinity);//PSU (ppt) 0.00 – 42.00
        values.add(specificGravity);//1.00 – 1.300
    }


    public double getConductivity() throws Exception{
        return getConductivity(true);
    }

    public double getConductivity(boolean withUpdate) throws Exception{
        return (withUpdate) ? update(0) :
         conductivity.getValue();
    }

    public double getTotalDissolvedSolids() throws Exception{
        return getTotalDissolvedSolids(true);
    }

    public long getTotalDissolvedSolids(boolean withUpdate) throws Exception{
        return (withUpdate) ? Math.round(update(1)) :
        totalDissolvedSolids.getValue();
    }

    public double getSalinity() throws Exception{
        return getSalinity(true);
    }

    public double getSalinity(boolean withUpdate) throws Exception{
        return (withUpdate) ? update(2) :
         salinity.getValue();
    }

    public double getSpecificGravity() throws Exception{
        return getSpecificGravity(true);
    }

    public double getSpecificGravity(boolean withUpdate) throws Exception{
        return (withUpdate) ?  update(3) :
        specificGravity.getValue();
    }

    private double update(int valueNumber) throws Exception{
        String[] value = readln("R", 600).split(",");
        if (value.length > 0) conductivity.setValue(Double.parseDouble(value[0]));
        if (value.length > 1) totalDissolvedSolids.setValue(Long.parseLong(value[1]));
        if (value.length > 2) salinity.setValue(Double.parseDouble(value[2]));
        if (value.length > 3) specificGravity.setValue(Double.parseDouble(value[3]));
        if (value.length > valueNumber)
            return Double.parseDouble(value[valueNumber]);
        else
            throw new Exception();
    }

    @Override
    public String toString() {
        String separator = "\n";
        return
        conductivity.toString() + separator +
        totalDissolvedSolids.toString() + separator +
        salinity.toString() + separator +
        specificGravity.toString();
    }

    @Override
    public String getClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }

}
