package ru.staffbots.tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.PageType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Обеспечивает мультиязычность приложения
 */
public class Translator {

    public static String languageCode = "ru";

    /**
     * Переводы,
     * languageCode -> xmlDocument
     */
    private static Document xmlDocument;

    /**
     * Значения,
     * заполняется при первом обращении, нужны для ускорения
     * Map<noteType:noteName,     noteData    >   (for journal)
     * Map<pageName:variableName, variableData>   (for pages)
     *
     * Map<dataName,              dataValue   >   (in common case)
     */
    private static Map<String, Map<String, Object>> data = new HashMap();

    private static XPath xPath;

    /**
     * Инициализация
     **/
    public static void init(){
        try {
//            Properties property = new Properties();
//            property.load(Resources.getAsStream("properties"));
//            languageCode = property.getProperty("staffbot.language", languageCode);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            xPath = xPathFactory.newXPath();
            InputStream inputStream = Resources.getAsStream("xml/" + languageCode + ".xml");
            xmlDocument = builder.parse(inputStream);

            for (PageType pageType : PageType.values())
                readData("pages", pageType.getName());

            for (NoteType noteType : NoteType.values())
                readData("journal", noteType.getName());

            for (String enumName : new String[]{"dateaccuracy", "notetype", "userrole", "tasksstatus"})
                readData("enums", enumName);

            for (String section : new String[]{"frame", "database"})
                readData("application", section);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readData(String path, String section) {
        String text = get("/language/" + path + "/" + section, null);
        if (text == null) return;
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(text));
        } catch (Exception e) {
            return;
        }
        Map<String, Object> sectionData = new HashMap();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value.trim().equals("")) value = key;
            sectionData.put(key, value);
        }
        data.put(section, sectionData);
    }

    private static String get(String path, String defaultValue){
        try {
            return xPath.evaluate(path, xmlDocument).trim();
        } catch (XPathExpressionException e) {
            return defaultValue;
        }
    }

    public static String getValue(String section, String key){
        if (!data.containsKey(section)) return key;
        if (!data.get(section).containsKey(key)) return key;
        return data.get(section).get(key).toString();
    }

    public static Map<String, Object> getSection(String section){
        return new HashMap(data.get(section));
    }

}
