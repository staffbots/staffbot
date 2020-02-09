package ru.staffbots.tools;

import freemarker.template.Configuration;
import freemarker.template.Template;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.resources.Resources;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public interface TemplateFillable {
    /**
     * Заполняет html-шаблон данными
     */
    default String fillTemplate(String fileName, Map<String, Object> data) {
        Writer stream = new StringWriter();
        try {
            Configuration conf = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            String codePage = "UTF-8";
            conf.setDefaultEncoding(codePage);
            InputStream inputStream = Resources.getAsStream(fileName);
            Charset charset = StandardCharsets.UTF_8;
            Template template = new Template(fileName, new InputStreamReader(inputStream, charset), conf, codePage);
            template.process(data, stream);
        } catch (Exception e) {
            Journal.add(e.getMessage());
        }
        return stream.toString();
    }

}
