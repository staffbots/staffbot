package ru.staffbot.utils.levers;

import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.values.DateValue;

import java.util.Date;

// Поле ввода даты/времени в указанном формате format
public class DateLever extends DateValue implements Lever  {


    public DateLever(String name, String note, Boolean dbStorage, DateFormat format, String value) {
        super(name, note, dbStorage, format, Converter.stringToDate(value, format));
    }

    public DateLever(String name, String note, DateFormat format, String value) {
        super(name, note, format, Converter.stringToDate(value, format));
    }

    public DateLever(String name, String note, Boolean dbStorage, LeverMode mode, DateFormat format, String value) {
        super(name, note, dbStorage, format, Converter.stringToDate(value, format));
        this.mode = mode;
    }

    public DateLever(String name, String note, LeverMode mode, DateFormat format, String value) {
        super(name, note, format, Converter.stringToDate(value, format));
        this.mode = mode;
    }


}
