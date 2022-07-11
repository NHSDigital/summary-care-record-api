package uk.nhs.adaptors.scr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import uk.nhs.adaptors.scr.clients.identity.sds.SdsClient;
import uk.nhs.adaptors.scr.clients.sds.SdsJSONResponseHandler;
import uk.nhs.adaptors.scr.config.SdsConfiguration;
import uk.nhs.adaptors.scr.models.PractitionerRoleResponse;

import java.net.URISyntaxException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SdsService {

    private static final String USER_ROLE_ID_FHIR_IDENTIFIER = "https://fhir.nhs.uk/Id/nhsJobRoleCode";
    private final SdsConfiguration sdsConfiguration;
    private final SdsClient sdsClient;
    private final SdsJSONResponseHandler sdsJSONResponseHandler;

    public String getUserRoleCode(String nhsdSessionUrid) throws URISyntaxException {

        var baseUrl = sdsConfiguration.getBaseUrl();

        var userRoleId = USER_ROLE_ID_FHIR_IDENTIFIER + "|" + nhsdSessionUrid;

        var uri = new URIBuilder(baseUrl + "/PractitionerRole")
            .setScheme("http")
            .addParameter("user-role-id", userRoleId)
            .build();

        WebClient client = WebClient.create();
        WebClient.ResponseSpec responseSpec = client.get()
            .uri(uri)
            .retrieve();

        LOGGER.info(responseSpec.bodyToMono(String.class).block());

        PractitionerRoleResponse response = responseSpec.bodyToMono(PractitionerRoleResponse.class).block();

        if (response == null || response.getEntry().isEmpty()) {
            LOGGER.info("response was empty");
            return "";
        }

        var entry = response.getEntry().get(0);
        var resource = entry.getResource();
        var roleCodes = resource.getCode();

        if (roleCodes.isEmpty()) {
            LOGGER.info("role codes was empty");
            return "";
        }

        LOGGER.info("roleCode: " + roleCodes.get(0));
        return roleCodes.get(0);
    }
}
