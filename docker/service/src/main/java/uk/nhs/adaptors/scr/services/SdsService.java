package uk.nhs.adaptors.scr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.sds.SdsClient;
import uk.nhs.adaptors.scr.config.SdsConfiguration;

import java.net.URISyntaxException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SdsService {

    private static final String USER_ROLE_ID_FHIR_IDENTIFIER = "https://fhir.nhs.uk/Id/nhsJobRoleCode";
    private final SdsConfiguration sdsConfiguration;
    private final SdsClient sdsClient;

    public String getUserRoleCode(String nhsdSessionUrid) throws URISyntaxException {

        var baseUrl = sdsConfiguration.getBaseUrl();

        var userRoleId = USER_ROLE_ID_FHIR_IDENTIFIER + "|" + nhsdSessionUrid;

        var uri = new URIBuilder(baseUrl + "/PractitionerRole")
            .setScheme("http")
            .addParameter("user-role-id", userRoleId)
            .build();

        var response = sdsClient.sendGet(uri);

        return getCodeFromBundle(response);
    }

    private String getCodeFromBundle(Bundle bundle) {

        if (bundle == null || !bundle.hasEntry()) {
            return "";
        }

        var entry = bundle.getEntryFirstRep();

        if (!entry.hasResource()) {
            return "";
        }

        var resource = (PractitionerRole) entry.getResource();

        if (!resource.hasCode()) {
            return "";
        }

        var roleCode = resource.getCodeFirstRep();

        if (!roleCode.hasCoding()) {
            return "";
        }

        return roleCode.getCodingFirstRep().getCode();
    }
}
