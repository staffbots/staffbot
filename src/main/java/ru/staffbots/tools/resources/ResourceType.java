package ru.staffbots.tools.resources;


/**
 *
 */
public enum ResourceType {

    HTML,
    JS,
    CSS,
    IMG,
    UNKNOWN;

    @Override
    public String toString(){
        return super.toString().toLowerCase();
    }

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
