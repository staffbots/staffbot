package ru.staffbots.tools.levers;

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
            if (lever.toValue().isStorable())
                lever.toValue().createTable();
            lever.toValue().set(lever.toValue().get());
        }
    }

    public static void reset(){
//        for (Lever lever: Levers.list)
//            lever.reset();
    }

}
