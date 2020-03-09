package ru.staffbots.tools;

import java.util.Properties;

/**
 * Properties class extension with additional conversion functions
 */
public class ParsableProperties extends Properties {

    public double getDoubleProperty(String key, double defaultValue){
        String stringValue = getProperty(key, String.valueOf(defaultValue));
        try {
            return Double.parseDouble(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getIntegerProperty(String key, int defaultValue){
        String stringValue = getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Boolean getBooleanProperty(String key, Boolean defaultValue){
        String trueString = "true";
        String falseString = "false";
        String stringValue = getProperty(key, defaultValue.toString());
        if(stringValue.equalsIgnoreCase(trueString))
            return true;
        else
            if(stringValue.equalsIgnoreCase(falseString))
                return false;
            else
                return defaultValue;
    }

    public String getProperty(String key, String defaultValue){
        return super.getProperty(key, defaultValue).trim();
    }

}
