package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.EmptyValue;

/**
 * Специальный класс-разделитель, используется для группировки рычагов управления
 */
public class EmptyLever extends EmptyValue implements Lever {

    public EmptyLever(String note) {
        super(note);
    }

}
