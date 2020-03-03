package ru.staffbots.tools.resources;


/**
 *
 */
public enum ResourceType {

    CSS,
    HTML,
    IMG,
    INO,
    JS,
    XML,
    UNKNOWN;

    public static ResourceType getByName(String resourceName) {
        if (resourceName == null)
            return UNKNOWN;
        int index = resourceName.indexOf("/");
        if (index < 1)
            return UNKNOWN;
        for (ResourceType type : values())
            if (type.toString().equalsIgnoreCase(resourceName.substring(0,index)))
                return type;
        return UNKNOWN;
    }
}
