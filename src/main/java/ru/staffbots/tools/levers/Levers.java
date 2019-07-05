package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.Value;

import java.util.ArrayList;

/**
 *
 */
public class Levers{

    /**
     * <b>Список рычагов управления</b>, тех что отображаются на закладке "Управление"
     */
    public static ArrayList<Lever> list = new ArrayList<>(0);

    public static void initGroup(String groupName, Lever... levers) {
        list.add(new GroupLever(groupName));
        init(levers);
    }

    public static void init(Lever... levers) {
        for (Lever lever:levers) {
            list.add(lever);
            Value value = lever.toValue();
            if (value.isStorable()) {
                value.createTable();
                value.set(value.get());
            }
        }
    }

    public static void reset(){
//        for (Lever lever: Levers.list)
//            lever.reset();
    }

}
