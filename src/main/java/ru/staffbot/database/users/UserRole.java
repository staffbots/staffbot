package ru.staffbot.database.users;

import java.util.HashMap;
import java.util.Map;

public enum UserRole {

    ADMIN(0, "Администратор"),
    MANAGER(1, "Управляющий"),
    INSPECTOR(2, "Инспектор");

    private String description;
    private int accessLevel;
    private static Map accessLevelMap = new HashMap<>();
    private static Map nameMap = new HashMap<>();

    private static final UserRole defaultRole = UserRole.INSPECTOR;

    UserRole(int accessLevel, String description) {
        this.accessLevel = accessLevel;
        this.description = description;
    }


    public String getDescription(){
        return description;
    }

    static {
        for (UserRole userRole : UserRole.values()) {
            accessLevelMap.put(userRole.accessLevel, userRole);
            nameMap.put(userRole.name(), userRole);
        }
    }

    public static UserRole valueOf(int accessLevel) {
        return accessLevelMap.containsKey(accessLevel) ? (UserRole) accessLevelMap.get(accessLevel) : defaultRole;
    }

    public static UserRole valueByName(String name) {

        return nameMap.containsKey(name) ? (UserRole) nameMap.get(name) : defaultRole;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public String getName() {
        return name().toLowerCase();
    }

}
