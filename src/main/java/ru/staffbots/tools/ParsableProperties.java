package ru.staffbots.tools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Properties class extension with additional conversion functions
 */
public class ParsableProperties extends Properties {

    public Double getDoubleProperty(String key, Double defaultValue){
        String defaultStringValue = (defaultValue == null) ? null : String.valueOf(defaultValue);
        String stringValue = getProperty(key, defaultStringValue);
        if (stringValue == null)
            return defaultValue;
        try {
            return Double.parseDouble(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Double getDoubleProperty(String key){
        return getDoubleProperty(key, null);
    }

    public Double getDoubleProperty(String key, double defaultValue){
        return getDoubleProperty(key, new Double(defaultValue));
    }

    public Integer getIntegerProperty(String key, Integer defaultValue){
        String defaultStringValue = (defaultValue == null) ? null : String.valueOf(defaultValue);
        String stringValue = getProperty(key, defaultStringValue);
        if (stringValue == null)
            return defaultValue;
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Integer getIntegerProperty(String key){
        return getIntegerProperty(key, null);
    }

    public Integer getIntegerProperty(String key, int defaultValue){
        return getIntegerProperty(key, new Integer(defaultValue));
    }

    public Boolean getBooleanProperty(String key, Boolean defaultValue){
        String trueString = "true";
        String falseString = "false";
        String defaulStringValue = (defaultValue == null) ? null : (defaultValue ? trueString : falseString);
        String stringValue = getProperty(key, defaulStringValue);
        if (stringValue == null)
            return defaultValue;
        if(stringValue.equalsIgnoreCase(trueString))
            return true;
        if(stringValue.equalsIgnoreCase(falseString))
            return false;
        return defaultValue;
    }

    public Boolean getBooleanProperty(String key){
        return getBooleanProperty(key, null);
    }

    public Boolean getBooleanProperty(String key, boolean defaultValue){
        return getBooleanProperty(key, new Boolean(defaultValue));
    }

    @Override
    public String getProperty(String key){
        return getProperty(key, null);
    }

    @Override
    public String getProperty(String key, String defaultValue){
        String property = (defaultValue == null) ? super.getProperty(key) : super.getProperty(key, defaultValue);
        return (property == null) ? null : property.trim();
    }

    public String[] getStringsProperty(String key){
        return getStringsProperty(key, ",");
    }

    public String[] getStringsProperty(String key, String separator){
        List<String> result = new ArrayList();
        String[] values = getProperty(key, "").split(separator);
        for(String value: values)
            if (!value.trim().isEmpty())
                result.add(value.trim());
        return result.toArray(new String[result.size()]);
    }

}
