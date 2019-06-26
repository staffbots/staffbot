package ru.staffbot.tools;

import ru.staffbot.tools.dates.DateFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <b>Конвертер</b><br>
 * Содержит всевозможные функции конвертирования типов
 */
// Следует приобщить преобразование типов к самим типам
// т.е. включить эти методы в классы DoubleValue и т.д.
public class Converter {

    public static byte[] longToBytes(long value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);
        return bytes;
    }

    public static long bytesToLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static byte[] inputStreamToBytes(InputStream inputStream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException exception) {
            return new byte[0];
        }
    }

    public static byte[] doubleToBytes(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static double bytesToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static Date longToDate(long value) {
        return new Date(value); // Задаем количество миллисекунд
    }

    public static long dateToLong(Date value) {
        return value.getTime(); // Узнаём количество миллисекунд
    }

    public static double longToDouble(long value) {
        return bytesToDouble(longToBytes(value));
    }

    public static long doubleToLong(double value) {
        return bytesToLong(doubleToBytes(value));
    }

    public static double dateToDouble(Date value) {
        return longToDouble(dateToLong(value));
    }

    public static Date doubleToDate(Double value) {
        return longToDate(doubleToLong(value));
    }

    public static long booleanToLong(boolean value) {
        return (value ? 1 : 0);
    }

    public static boolean longToBoolean(long value) {
        return (value > 0.5);
    }

    public static Date stringToDate(String value, DateFormat format){
        //Date date = stringToDate(value, format, new Date());
        return stringToDate(value, format, new Date());
    }

    public static Date stringToDate(String value, DateFormat format, Date defaultDate){
        if (value == null) return defaultDate;
        SimpleDateFormat simpleFormat = new SimpleDateFormat();
        simpleFormat.applyPattern(format.get());
        try {
            return simpleFormat.parse(value);
        } catch (Exception exception) {
            return defaultDate;
        }
    }

    public static String dateToString(Date value, DateFormat format, String defaultValue){
        if (value == null) return defaultValue;
        SimpleDateFormat simpleFormat = new SimpleDateFormat();
        simpleFormat.applyPattern(format.get());
        return simpleFormat.format(value);
    }

    public static String dateToString(Date value, DateFormat format){
        return dateToString(value, format, "");
    }

    public static long[] doublesToLongs(Double... values) {
        long[] longValues = new long[values.length];
        for (int i = 0; i < values.length; i++)
            longValues[i] = Converter.doubleToLong(values[i]);
        return longValues;
    }

}
