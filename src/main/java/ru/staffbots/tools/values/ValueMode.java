package ru.staffbots.tools.values;

/*
 * Режимы работы Value и его потомков,
 * определяеющие будет ли фиксироваться в БД история изменения значения
 */
public enum ValueMode {

    STORABLE, // Режим работы Value, при котором каждое изменение значения фиксируется в БД
    TEMPORARY // Режим работы Value, при котором значение в БД не пишется

}
