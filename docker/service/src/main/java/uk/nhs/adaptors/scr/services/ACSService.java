package uk.nhs.adaptors.scr.services;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import uk.nhs.adaptors.scr.models.ACSPayload;

@Component
public class ACSService {

    private static final String SET_RESOURCE_PERMISSIONS_TEMPLATE_PATH = "set_resource_permissions_template.mustache";
    private static final Mustache SET_RESOURCE_PERMISSIONS_TEMPLATE = loadSuccessTemplate();

    private static Mustache loadSuccessTemplate() {
        return getResourceAsString(SET_RESOURCE_PERMISSIONS_TEMPLATE_PATH);
    }

    private static Mustache getResourceAsString(String path) {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile(path);
        return m;
    }

    public String handleMessage(ACSPayload acsSetResourceObject) throws IOException {

        StringWriter writer = new StringWriter();
        String setResourcePermissions = "";

        Map<String, Object> context = new HashMap<>();
        context.put("ACSPayload", acsSetResourceObject.getPayload());

        try {
            SET_RESOURCE_PERMISSIONS_TEMPLATE.execute(writer, context).flush();
            setResourcePermissions += writer.toString();
        } catch (IOException e) {
            throw new IOException(e);
        }

        return setResourcePermissions;
    }
}
