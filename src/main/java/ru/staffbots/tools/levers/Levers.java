package ru.staffbots.tools.levers;

import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.values.ValueType;

import java.io.StringReader;
import java.util.*;

/**
 * Levers set, singleton class
 */
public class Levers extends ArrayList<Lever> {

    private Levers() {
        super(0);
    }

    private static final Levers instance = new Levers();

    private static void initValue(Value value) {
        if (value.isStorable()) {
            value.createTable();
            value.set(value.get());
        }
    }

    public static boolean addLever(Lever lever) {
        if (instance.contains(lever)) return false;
        boolean result = instance.add(lever);
        if (result) lever.toValue().dbInit();
        return result;
    }

    public static int addLevers(Lever... levers) {
        int result = 0;
        for (Lever lever:levers)
            if (addLever(lever)) result++;
        return result;
    }

    public static boolean addGroup(String groupName) {
        return addLever(new GroupLever(groupName));
    }

    public static int addGroup(String groupName, Lever... levers) {
        if (!addGroup(groupName)) return 0;
        return addLevers(levers) + 1;
    }

    public static int addObjects(Object... levers) {
        int result = 0;
        for (Object lever:levers) {
            if (lever == null) continue;
            if (lever instanceof String)
                if (!addGroup((String) lever)) continue;
            if (lever instanceof Lever)
                if (!addLever((Lever) lever)) continue;
            result ++;
        }
        return result;
    }

    public static ArrayList<Lever> getList() {
        return instance;
    }

    public static int getMaxStringValueSize(){
        int maxSize = 0;
        for (Lever lever: instance) {
            if (!lever.toValue().getValueType().isSizeble()) continue;
            int size = lever.toValue().getStringValueSize();
            if (size > maxSize) maxSize = size;
        }
        return maxSize;
    }

    public static ArrayList<ButtonLever> getButtonList(){
        ArrayList<ButtonLever> result = new ArrayList();
        for (Lever lever: instance)
            if (lever.isButton())
                try {
                    ButtonLever buttonLever = (ButtonLever)lever;
                    result.add(buttonLever);
                } catch (Exception exception) {
                    // Игнорируем
                }
        return result;
    }

    public static String toConfigValue(){
        Map<String, String> nameValues = new HashMap<>();
        Value value;
        for (Lever lever : instance) {
            if (!lever.isChangeable()) continue;
            value = lever.toValue();
            if (value.typeIs(ValueType.VOID)) continue;
            nameValues.put(value.getName(), Long.toString(value.get()));
        }
        String rawConfigValue = nameValues.toString();
        return rawConfigValue.substring(1, rawConfigValue.length() - 1).replace(", ", "\n");
    }

    public static boolean fromConfigValue(String configValue){
        if (configValue == null) return false;
        Properties properties = new Properties();
        try {
            properties.load(
                    new StringReader(
                            configValue));
        } catch (Exception e) {
            return false;
        }
        for (Lever lever : instance)
            if (lever.toValue().getValueType() != ValueType.VOID)
                if (properties.containsKey(lever.getName()))
                    lever.set(
                            Long.parseLong(
                                    properties.getProperty(
                                            lever.getName())));
        return true;
    }

}
