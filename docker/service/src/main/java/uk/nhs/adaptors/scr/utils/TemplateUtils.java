package uk.nhs.adaptors.scr.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.SneakyThrows;

import java.io.StringWriter;

public class TemplateUtils {
    private static final String TEMPLATES_DIRECTORY = "templates";

    public static Mustache loadTemplate(String templateName) {
        MustacheFactory mf = new DefaultMustacheFactory(TEMPLATES_DIRECTORY);
        Mustache m = mf.compile(templateName);
        return m;
    }

    @SneakyThrows
    public static String fillTemplate(Mustache template, Object content) {
        StringWriter writer = new StringWriter();
        String data = "";

        template.execute(writer, content).flush();
        data += writer.toString();

        return data;
    }
}
