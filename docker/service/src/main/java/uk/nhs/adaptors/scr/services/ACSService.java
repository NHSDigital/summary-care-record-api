package uk.nhs.adaptors.scr.services;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.exceptions.SoapClientException;
import uk.nhs.adaptors.scr.models.ACSPayload;
import uk.nhs.adaptors.scr.models.responses.ConsentsResponse;
import uk.nhs.adaptors.scr.utils.AcsResponseParserUtil;

@Component
public class AcsService {
    @Autowired
    private SpineClient spineClient;

    private static final String TEMPLATES_DIRECTORY = "templates";
    private static final String SET_RESOURCE_PERMISSIONS_TEMPLATE_NAME = "set_resource_permissions.mustache";
    private static final String GET_RESOURCE_PERMISSIONS_TEMPLATE_NAME = "get_resource_permissions.mustache";

    public void setResourcePermissions(ACSPayload acsSetResourceObject) {
        Map<String, Object> context = new HashMap<>();
        context.put("ACSPayload", acsSetResourceObject.getPayload());

        String acsRequest = prepareAcsRequest(SET_RESOURCE_PERMISSIONS_TEMPLATE_NAME, context);
        spineClient.sendAcsRequest(acsRequest);
    }

    public ConsentsResponse getResourcePermissions(int patientId) throws SoapClientException, DocumentException {
        Map<String, Object> context = new HashMap<>();
        context.put("patientId", patientId);

        String acsRequest = prepareAcsRequest(GET_RESOURCE_PERMISSIONS_TEMPLATE_NAME, context);
        String acsResponse = spineClient
            .sendAcsRequest(acsRequest)
            .getBody();

        ConsentsResponse response = new ConsentsResponse();
        response.setConsents(AcsResponseParserUtil.parseGetResourcePermissionsXml(acsResponse));
        return response;
    }

    private String prepareAcsRequest(String templateName, Object content) {
        MustacheFactory mf = new DefaultMustacheFactory(TEMPLATES_DIRECTORY);
        Mustache m = mf.compile(templateName);

        StringWriter writer = new StringWriter();
        String acsRequest = "";

        try {
            m.execute(writer, content).flush();
            acsRequest += writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return acsRequest;
    }
}
