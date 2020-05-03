package ru.staffbots.tools.languages;

import java.util.HashMap;
import java.util.Map;

public class Languages {

    public static String defaultCode = null;

    private static Map<String, Language> list = new HashMap();

    public static boolean put(String languageCode) {
        languageCode = languageCode.toLowerCase().trim();
        if (list.containsKey(languageCode))
            return false;
        list.put(languageCode, new Language(languageCode));
        if (defaultCode == null) defaultCode = languageCode;
        return true;
    }

    public static Language get() {
        return get(defaultCode);
    }

    public static Language get(String languageCode) {
        languageCode = languageCode.toLowerCase().trim();
        if (!list.containsKey(languageCode))
            languageCode = defaultCode;
        return  (list.containsKey(languageCode) ? list.get(languageCode) : null );
    }

    public static String[] getAllCodes() {
        return list.keySet().toArray(new String[list.keySet().size()]);
    }

    public static String getDefaultCode() {
        return  defaultCode;
    }

}
