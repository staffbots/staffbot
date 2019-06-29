package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.VoidValue;

/**
 * Специальный класс-разделитель, используется для группировки рычагов управления
 */
public class GroupLever extends VoidValue implements Lever {

    public GroupLever(String note) {
        super(note);
    }

}