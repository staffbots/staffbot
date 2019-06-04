package ru.staffbot.utils.levers;

import ru.staffbot.database.Database;
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
        list.add(new EmptyLever(groupName));
        init(levers);
    }

    public static void init(Lever... levers) {
        for (Lever lever:levers) {
            list.add(lever);
            if (lever.getDbStorage())
                Database.createValueTable("val_" + lever.getName());
            lever.set(lever.get());
        }
    }

    public static void reset(){
//        for (Lever lever: Levers.list)
//            lever.reset();
    }

}
