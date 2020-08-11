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

    public String handleMessage(ACSPayload acsSetResourceObject) {

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile(SET_RESOURCE_PERMISSIONS_TEMPLATE_PATH);

        StringWriter writer = new StringWriter();
        String setResourcePermissions = "";

        Map<String, Object> context = new HashMap<>();
        context.put("ACSPayload", acsSetResourceObject.getPayload());

        try {
            m.execute(writer, context).flush();
            setResourcePermissions += writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return setResourcePermissions;
    }
}
