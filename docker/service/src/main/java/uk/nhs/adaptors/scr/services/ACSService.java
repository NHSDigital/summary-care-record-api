package uk.nhs.adaptors.scr.services;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.models.ACSPayload;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ACSService {

    private static final String SET_RESOURCE_PERMISSIONS_TEMPLATE_PATH = "set_resource_permissions_template.mustache";
    private static final Mustache SET_RESOURCE_PERMISSIONS_TEMPLATE = loadAcsSetResourceTemplate();

    private final SpineClient spineClient;

    private static Mustache loadAcsSetResourceTemplate() {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache m = mf.compile(SET_RESOURCE_PERMISSIONS_TEMPLATE_PATH);
        return m;
    }

    public void sendAcsSetResource(ACSPayload acsSetResourceObject) throws IOException {
        String soapMessage = prepareSoapSetResourceMessage(acsSetResourceObject);
        spineClient.sendToACSEndpoint(soapMessage);
    }

    private String prepareSoapSetResourceMessage(ACSPayload acsSetResourceObject) throws IOException {
        StringWriter writer = new StringWriter();
        Map<String, Object> context = new HashMap<>();
        context.put("ACSPayload", acsSetResourceObject.getPayload());

        SET_RESOURCE_PERMISSIONS_TEMPLATE.execute(writer, context).flush();
        String soapMessage = writer.toString();

        return soapMessage;
    }
}
