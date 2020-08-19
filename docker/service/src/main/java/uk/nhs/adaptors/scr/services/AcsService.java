package uk.nhs.adaptors.scr.services;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.mustachejava.Mustache;

import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.models.ACSPayload;
import uk.nhs.adaptors.scr.models.responses.ConsentsResponse;
import uk.nhs.adaptors.scr.utils.AcsResponseParser;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

@Component
public class AcsService {
    @Autowired
    private SpineClient spineClient;

    private static final Mustache SET_RESOURCE_PERMISSIONS_TEMPLATE = TemplateUtils.loadTemplate("set_resource_permissions.mustache");
    private static final Mustache GET_RESOURCE_PERMISSIONS_TEMPLATE = TemplateUtils.loadTemplate("get_resource_permissions.mustache");

    public void setResourcePermissions(ACSPayload acsSetResourceObject) {
        Map<String, Object> context = new HashMap<>();
        context.put("ACSPayload", acsSetResourceObject.getPayload());

        String acsRequest = TemplateUtils.fillTemplate(SET_RESOURCE_PERMISSIONS_TEMPLATE, context);
        spineClient.sendAcsRequest(acsRequest);
    }

    public ConsentsResponse getResourcePermissions(int patientId) throws DocumentException {
        Map<String, Object> context = new HashMap<>();
        context.put("patientId", patientId);

        String acsRequest = TemplateUtils.fillTemplate(GET_RESOURCE_PERMISSIONS_TEMPLATE, context);
        String acsResponse = spineClient
            .sendAcsRequest(acsRequest)
            .getBody();

        ConsentsResponse response = new ConsentsResponse();
        response.setConsents(AcsResponseParser.parseGetResourcePermissionsXml(acsResponse));
        return response;
    }
}
