package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.values.ValueType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Levers{

    /**
     * <b>Список рычагов управления</b>, тех что отображаются на закладке "Управление"
     */
    public static ArrayList<Lever> list = new ArrayList();

    public static void initGroup(String groupName, Lever... levers) {
        list.add(new GroupLever(groupName));
        init(levers);
    }

    public static void init(Lever... levers) {
        for (Lever lever:levers) {
            if (list.contains(lever)) continue;
            list.add(lever);
            Value value = lever.toValue();
            if (value.isStorable()) {
                value.createTable();
                value.set(value.get());
            }
        }
    }

    public static void init(Object... levers) {
        if (levers == null) levers = new Object[0];
        for (Object lever:levers) {
            if ((lever == null)||(lever instanceof String))
                initGroup((String) lever);
            if (lever instanceof Lever)
                init((Lever) lever);
        }
    }

    public static void reset(){
        for (Lever lever: Levers.list)
            lever.toValue().reset();
    }

    public static int getMaxStringValueSize(){
        int maxSize = 0;
        for (Lever lever: Levers.list) {
            if (!lever.toValue().getValueType().isSizeble()) continue;
            int size = lever.toValue().getStringValueSize();
            if (size > maxSize) maxSize = size;
        }
        return maxSize;
    }

    public static Map<String, String> getNameValues(){
        Map<String, String> nameValues = new HashMap<>();
        for (Lever lever : list)
            if (!lever.isGroup())
                nameValues.put(
                    lever.toValue().getName(),
                    Long.toString(lever.toValue().get()));
        return nameValues;
    }

    public static ArrayList<ButtonLever> getButtonList(){
        ArrayList<ButtonLever> result = new ArrayList();
        for (Lever lever: Levers.list)
            if (lever.isButton())
                try {
                    ButtonLever buttonLever = (ButtonLever)lever;
                    result.add(buttonLever);
                } catch (Exception exception) {
                    // Игнорируем
                }
        return result;
    }


}
