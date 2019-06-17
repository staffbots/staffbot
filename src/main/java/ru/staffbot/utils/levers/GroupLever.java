package ru.staffbot.utils.levers;

import ru.staffbot.utils.values.VoidValue;

/**
 * Специальный класс-разделитель, используется для группировки рычагов управления
 */
public class GroupLever extends VoidValue implements Lever {

    public GroupLever(String note) {
        super(note);
    }

}