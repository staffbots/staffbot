package ru.staffbots.tools;

import java.util.Properties;

/**
 * Properties class extension with additional conversion functions
 */
public class ParsableProperties extends Properties {

    public Integer getIntegerProperty(String key, Integer defaultValue){
        Integer value;
        String stringValue = getProperty(key, defaultValue.toString());
        try {
            value = Integer.valueOf(stringValue);
        } catch (NumberFormatException e) {
            value = defaultValue;
        }
        return value;
    }

    public Boolean getBooleanProperty(String key, Boolean defaultValue){
        String trueString = "true";
        String falseString = "false";
        Boolean value;
        String stringValue = getProperty(key, defaultValue.toString());
        if(stringValue.equalsIgnoreCase(trueString))
            value = true;
        else
            if(stringValue.equalsIgnoreCase(falseString))
                value = false;
            else
                value = defaultValue;
        return value;
    }

    public String getProperty(String key, String defaultValue){
        return super.getProperty(key, defaultValue).trim();
    }
}
