package ru.staffbots.tools.languages;

import org.w3c.dom.Document;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.PageType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Language, init by xml-file
 */
public class Language {

    private static final String defaultSection = "general";

    private String code;

    private String title;

    private Map<String, Map<String, Object>> data = new HashMap();

    private Document xmlDocument;

    private XPath xPath;

    public Language(String code){
        try {
            this.code = code;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            XPathFactory xPathFactory = XPathFactory.newInstance();
            xPath = xPathFactory.newXPath();
            InputStream inputStream = Resources.getAsStream("xml/" + code + ".xml");
            xmlDocument = builder.parse(inputStream);
            title = xmlDocument.getDocumentElement().getAttribute("title");

            for (PageType pageType : PageType.values())
                readData("pages", pageType.getName());

            for (NoteType noteType : NoteType.values())
                readData("journal", noteType.getName());

            for (String enumName : new String[]{"dateaccuracy", "notetype", "userrole", "tasksstatus"})
                readData("enums", enumName);

            for (String section : new String[]{defaultSection, "frame", "database"})
                readData("application", section);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readData(String path, String section) {
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

    private String get(String path, String defaultValue){
        try {
            return xPath.evaluate(path, xmlDocument).trim();
        } catch (XPathExpressionException e) {
            return defaultValue;
        }
    }

    public String getCode(){
        return code;
    }

    public String getTitle(){
        return title;
    }

    public String getValue(String key){
        return getValue(defaultSection, key);
    }

    public String getValue(String section, String key){
        if (!data.containsKey(section)) return key;
        if (!data.get(section).containsKey(key)) return key;
        return data.get(section).get(key).toString();
    }

    public Map<String, Object> getSection(String section){
        return new HashMap(data.get(section));
    }

    @Override
    public String toString() {
        return getCode();
    }
}
