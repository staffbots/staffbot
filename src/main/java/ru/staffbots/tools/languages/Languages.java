package ru.staffbots.tools.languages;

import ru.staffbots.database.settings.Settings;

import java.util.HashMap;

public class Languages extends HashMap<String, Language> {

    private Languages() {
        super();
    }

    private static final Languages instance = new Languages();

    private static String defaultCode = null;

    public static boolean put(String languageCode) {
        if (languageCode == null) return false;
        languageCode = languageCode.toLowerCase().trim();
        if (instance.containsKey(languageCode)) return false;
        instance.put(languageCode, new Language(languageCode));
        if (defaultCode == null) setDefaultCode(languageCode);
        return true;
    }

    public static Language get() {
        return get(defaultCode);
    }

    public static Language get(String languageCode) {
        if (languageCode == null)
            languageCode = defaultCode;
        if (languageCode == null)
            return null;
        languageCode = languageCode.toLowerCase().trim();
        if (!instance.containsKey(languageCode))
            languageCode = defaultCode;
        return  (instance.containsKey(languageCode) ? instance.get((Object)languageCode) : null );
    }

    public static String[] getAllCodes() {
        return instance.keySet().toArray(new String[instance.keySet().size()]);
    }

    public static String getDefaultCode() {
        return  defaultCode;
    }

    public static void loadDefaultCode() {
        setDefaultCode(Settings.loadAsString("default_language_code", defaultCode));
    }

    public static boolean setDefaultCode(String languageCode) {
        if (languageCode == null) return false;
        languageCode = languageCode.toLowerCase().trim();
        if (instance.containsKey(languageCode)) defaultCode = languageCode;
        Settings.save("default_language_code", defaultCode);
        return true;
    }

}
