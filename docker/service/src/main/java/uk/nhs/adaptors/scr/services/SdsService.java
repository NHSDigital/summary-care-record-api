package uk.nhs.adaptors.scr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

        var request = new HttpGet();

        var uri = new URIBuilder(sdsConfiguration.getBaseUrl() + "/PractitionerRole")
            .addParameter("UserRoleId", nhsdSessionUrid)
            .setScheme("tcp")
            .build();

        request.setURI(uri);

        var response = sdsClient.sendRequest(request, sdsJSONResponseHandler);

        return response.getBody();
    }
}
