package ru.staffbots.webserver.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.resources.ResourceType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Предоставляет функцию, которая заполняет html-шаблон данными
 * а так же кодирует русские буквы в строку с кодом UTF-8
 */
public class PageGenerator {

//    public static String getPage(String filename, Map<String, Object> data) {
//        return getPage(ResourceType.HTML, "/html/" + filename, data);
//    }

    public static String getPage(String filename, Map<String, Object> data) {
        Writer stream = new StringWriter();
        try {
            Configuration conf = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            String codePage = "UTF-8";
            conf.setDefaultEncoding(codePage);
            InputStream inputStream = PageGenerator.class.getResourceAsStream(filename);
            Charset charset = StandardCharsets.UTF_8;
            Template template = new Template(filename, new InputStreamReader(inputStream, charset), conf, codePage);
            template.process(data, stream);
            //data.clear();
        } catch (Exception e) {
            Journal.add(e.getMessage());
        }
        return stream.toString();
    }


}
