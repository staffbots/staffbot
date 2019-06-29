package ru.staffbots.tools.levers;

import ru.staffbots.tools.Converter;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.values.DateValue;
import ru.staffbots.tools.values.ValueMode;

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
