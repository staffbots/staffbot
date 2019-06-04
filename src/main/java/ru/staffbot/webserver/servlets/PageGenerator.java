package ru.staffbot.webserver.servlets;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import ru.staffbot.Staffbot;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Предоставляет функцию, которая заполняет html-шаблон данными
 * а так же кодирует русские буквы в строку с кодом UTF-8
 */
public class PageGenerator {

    private static final String HTML_DIR = "/html/";

    public static String getPage(String filename, Map<String, Object> data) {
        Writer stream = new StringWriter();
        try {
            Configuration conf = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            String codePage = "UTF-8";
            conf.setDefaultEncoding(codePage);
            InputStream inputStream = PageGenerator.class.getResourceAsStream(HTML_DIR + filename);
            Charset charset = StandardCharsets.UTF_8;
            Template template = new Template(filename,new InputStreamReader(inputStream, charset),conf, codePage);
            template.process(data, stream);
        } catch (Exception e) {
            Journal.add(e.getMessage());
            //e.printStackTrace();
        }
        return toCode(stream.toString());
        //return stream.toString();
    }

    private static String toCode(String data) {
        StringBuffer result = new StringBuffer("");
        for (int i = 0; i < data.length(); i++) {
            String c = data.substring(i, i + 1);
            result.append(c.matches("[а-яА-ЯёЁ]") ? "&#" + (int) c.charAt(0) : c);
        }
        return result.toString();
    }
}
