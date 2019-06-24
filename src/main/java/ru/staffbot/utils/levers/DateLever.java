package ru.staffbot.utils.levers;

import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.values.DateValue;
import ru.staffbot.utils.values.ValueMode;

import java.util.Date;

// Поле ввода даты/времени в указанном формате format
public class DateLever extends DateValue implements Lever  {


    public DateLever(String name, String note, ValueMode valueMode, DateFormat format, String value) {
        super(name, note, valueMode, format, Converter.stringToDate(value, format));
    }

    public DateLever(String name, String note, DateFormat format, String value) {
        super(name, note, format, Converter.stringToDate(value, format));
    }

    public DateLever(String name, String note, ValueMode valueMode, LeverMode leverMode, DateFormat format, String value) {
        super(name, note, valueMode, format, Converter.stringToDate(value, format));
        this.leverMode = leverMode;
    }

    public DateLever(String name, String note, LeverMode leverMode, DateFormat format, String value) {
        super(name, note, format, Converter.stringToDate(value, format));
        this.leverMode = leverMode;
    }


}
