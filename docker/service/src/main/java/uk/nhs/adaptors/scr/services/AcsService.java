package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.SpineClientContract;
import uk.nhs.adaptors.scr.clients.SpineHttpClient;
import uk.nhs.adaptors.scr.models.ACSPayload;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class AcsService {
    @Autowired
    private SpineClientContract spineClient;

    private static final Mustache SET_RESOURCE_PERMISSIONS_TEMPLATE = TemplateUtils.loadTemplate("set_resource_permissions.mustache");

    public SpineHttpClient.Response setResourcePermissions(ACSPayload acsSetResourceObject) {
        Map<String, Object> context = new HashMap<>();
        context.put("ACSPayload", acsSetResourceObject.getPayload());

        var acsRequest = TemplateUtils.fillTemplate(SET_RESOURCE_PERMISSIONS_TEMPLATE, context);
        return spineClient.sendAcsData(acsRequest);
    }
}
