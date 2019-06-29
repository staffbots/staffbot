package ru.staffbots.webserver.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import ru.staffbots.database.journal.Journal;

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

    public static String toCode(String data) {
        StringBuffer result = new StringBuffer("");
        for (int i = 0; i < data.length(); i++) {
            String c = data.substring(i, i + 1);
            result.append(c.matches("[а-яА-ЯёЁ]") ? "&#" + (int) c.charAt(0) : c);
        }
        return result.toString();
    }

    public static String fromCode(String data) {
        StringBuffer result = new StringBuffer("");
        String regex = "&#\\d{4};";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        int index = 0;
        while (matcher.find()) {
            int code = Integer.parseInt(data.substring(matcher.start() + 2, matcher.end()-1));
            if (matcher.start() != index)
                result.append(data.substring(index,matcher.start()));
            result.append((char)code);
            index = matcher.end();
        }
        result.append(data.substring(index, data.length()));
        return result.toString();
    }

}
