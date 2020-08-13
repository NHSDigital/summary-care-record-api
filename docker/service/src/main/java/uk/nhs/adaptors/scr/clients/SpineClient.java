package uk.nhs.adaptors.scr.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.scr.config.SpineConfiguration;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpineClient {
    private final SpineConfiguration spineConfiguration;

    public ResponseEntity<String> sendAcsRequest(String request) {
        return prepareRestTemplate()
            .postForEntity(spineConfiguration.getAcsEndpoint(), request, String.class);
    }

    private RestTemplate prepareRestTemplate() {
        var restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(spineConfiguration.getUrl()));
        return restTemplate;
    }
}
