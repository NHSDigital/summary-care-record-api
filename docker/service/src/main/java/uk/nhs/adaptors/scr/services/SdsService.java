package uk.nhs.adaptors.scr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import uk.nhs.adaptors.scr.clients.identity.sds.SdsClient;
import uk.nhs.adaptors.scr.clients.sds.SdsJSONResponseHandler;
import uk.nhs.adaptors.scr.config.SdsConfiguration;

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

        var baseUrl = "host.docker.internal:9001"; //sdsConfiguration.getBaseUrl();
        WebClient client = WebClient.create();

        WebClient client2 = WebClient.create();
        var uri = "http://"
            + baseUrl + "/healthcheck";

        WebClient.ResponseSpec responseSpec2 = client2.get()
            .uri(uri)
            .retrieve();
        String responseBody2 = responseSpec2.bodyToMono(String.class).block();

        var userRoleId = USER_ROLE_ID_FHIR_IDENTIFIER + "|" + nhsdSessionUrid;

        LOGGER.info(userRoleId + baseUrl);

//        var uri = new URIBuilder(baseUrl + "/PractitionerRole")
//            .setScheme("http")
//            .addParameter("user-role-id", userRoleId)
//            .build();

        var uri = "http://"
            + baseUrl + "/PractitionerRole?user-role-id=https://fhir.nhs.uk/Id/nhsJobRoleCode|555021935107";

        WebClient.ResponseSpec responseSpec = client.get()
            .uri(uri)
            .retrieve();
        String responseBody = responseSpec.bodyToMono(String.class).block();

        // add parameter in form user-role-id=https://fhir.nhs.uk/Id/nhsJobRoleCode|<NHSDSessionURID>

        LOGGER.info(responseBody);

        //var response = sdsClient.sendRequest(request, sdsJSONResponseHandler);

        return responseBody;
    }
}
