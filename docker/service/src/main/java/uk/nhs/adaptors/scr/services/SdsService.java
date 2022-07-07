package uk.nhs.adaptors.scr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.nhs.adaptors.scr.clients.identity.sds.SdsClient;
import uk.nhs.adaptors.scr.clients.sds.SdsJSONResponseHandler;
import uk.nhs.adaptors.scr.config.SdsConfiguration;

import java.net.URISyntaxException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SdsService {

    private final SdsConfiguration sdsConfiguration;
    private final SdsClient sdsClient;
    private final SdsJSONResponseHandler sdsJSONResponseHandler;

    public String getUserRoleCode(String nhsdSessionUrid) throws URISyntaxException {

        // var request = new HttpGet();

        // var uri = new URIBuilder(sdsConfiguration.getBaseUrl() + "/PractitionerRole")
        //     .addParameter("UserRoleId", nhsdSessionUrid)
        //     .build();

        // request.setURI(uri);

        // DEBUG
        // todo: delete me :)
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:9001/healthcheck";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        LOGGER.info(response.getBody());
        // DEBUG

        // DEBUG
        // WebClient client = WebClient.create(url);
        WebClient client = WebClient.create();

        WebClient.ResponseSpec responseSpec = client.get()
            .uri(url)
            .retrieve();
        String responseBody = responseSpec.bodyToMono(String.class).block();

        LOGGER.info(responseBody);
        // DEBUG

        url = "http://localhost:9001/PractitionerRole";
        response = restTemplate.getForEntity(url, String.class);

        // add parameter in form user-role-id=https://fhir.nhs.uk/Id/nhsJobRoleCode|<NHSDSessionURID>

        LOGGER.info(response.getBody());

        //var response = sdsClient.sendRequest(request, sdsJSONResponseHandler);

        return response.getBody();
    }
}
