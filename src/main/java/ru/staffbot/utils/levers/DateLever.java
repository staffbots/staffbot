package ru.staffbot.utils.levers;

import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.values.DateValue;

import java.util.Date;

// Поле ввода даты/времени в указанном формате format
public class DateLever extends DateValue implements Lever  {

    public DateLever(String name, String note, String value, DateFormat format, Boolean dbStorage) {
        super(name, note, Converter.stringToDate(value, format), format, dbStorage);
    }

    public DateLever(String name, String note, String value, DateFormat format) {
        super(name, note, Converter.stringToDate(value, format), format);
    }

}
