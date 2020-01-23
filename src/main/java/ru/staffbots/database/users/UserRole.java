package ru.staffbots.database.users;

import ru.staffbots.tools.Translator;

import java.util.HashMap;
import java.util.Map;

/**
 * User roles and some methods
 */
public enum UserRole {

    INSPECTOR(0),
    MANAGER(1),
    ADMIN(2);

    private int accessLevel;
    private static Map accessLevelMap = new HashMap<>();
    private static Map<String, UserRole> nameMap = new HashMap<>();
    private static Map descriptionMap = new HashMap<>();

    public static final UserRole defaultRole = UserRole.INSPECTOR;

    UserRole(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getDescription(){
        return Translator.getValue("userrole", getName());
    }

    static {
        for (UserRole userRole : UserRole.values()) {
            accessLevelMap.put(userRole.accessLevel, userRole);
            nameMap.put(userRole.getName(), userRole);
        }
    }

    public static UserRole valueOf(int accessLevel) {
        return accessLevelMap.containsKey(accessLevel) ? (UserRole) accessLevelMap.get(accessLevel) : defaultRole;
    }

    public static UserRole valueByName(String name) {
        if (name == null) return defaultRole;
        name = (name).toLowerCase();
        return nameMap.containsKey(name) ? nameMap.get(name) : defaultRole;
    }

    public static UserRole valueByDescription(String description) {
        if (descriptionMap.size() == 0)
            for (UserRole userRole : UserRole.values())
                descriptionMap.put(userRole.getDescription(), userRole);
        return descriptionMap.containsKey(description) ? (UserRole) descriptionMap.get(description) : defaultRole;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public String getName() {
        return name().toLowerCase();
    }

}
