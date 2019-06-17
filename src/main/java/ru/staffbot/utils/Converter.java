package ru.staffbot.utils;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <b>Конвертер</b><br>
 * Содержит всевозможные функции конвертирования типов и константы форматирования
 */
public class Converter {

    public static byte[] longToBytes(long value) {
//        ByteBuffer buffer = ByteBuffer.allocate(8);
//        buffer.putLong(value);
//        return buffer.array();
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);
        return bytes;
    }

    public static long bytesToLong(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.allocate(8);
//        buffer.put(value);
//        buffer.flip();
//        return buffer.getLong();
        return ByteBuffer.wrap(bytes).getLong();
    }

    public static byte[] doubleToBytes(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
//        ByteBuffer buffer = ByteBuffer.allocate(8);
//        buffer.putDouble(value);
//        return buffer.array();
    }

    public static double bytesToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
//        ByteBuffer buffer = ByteBuffer.allocate(8);
//        buffer.put(value);
//        buffer.flip();
//        return buffer.getDouble();
    }

    public static Date longToDate(long value) {
        //return new Date(value + Converter.stringToDate("00:00", "HH:mm").getTime()); // Задаем количество миллисекунд
        return new Date(value); // Задаем количество миллисекунд
    }

    public static long dateToLong(Date value) {
        return value.getTime(); // Узнаём количество миллисекунд
    }

    public static double longToDouble(long value) {
//        Long l = (new Long(value)).doubleValue();
//        double d = l.doubleValue();
//        return ((Long)value).doubleValue();
//        return Double.longBitsToDouble(value);
//        return value;
        return bytesToDouble(longToBytes(value));
    }

    public static long doubleToLong(double value) {
//        Double l = new Double(value);
//        double d = l.doubleValue();
//        return ((Double)value).longValue();
//        return Double.doubleToRawLongBits(value);
//        return Double.doubleToLongBits(value);
//        return (long) value;
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
        Date date = stringToDate(value, format, new Date());
        return stringToDate(value, format, new Date());
    }

    public static Date stringToDate(String value, DateFormat format, Date defaultDate){
        if (value == null) return defaultDate;
        SimpleDateFormat simpleFormat = new SimpleDateFormat();
        simpleFormat.applyPattern(format.get());
        try {
            return simpleFormat.parse(value);
        } catch (ParseException e) {
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
