package uk.nhs.adaptors.scr.clients;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import uk.nhs.adaptors.scr.config.SpineConfiguration;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpineClient {
    private final SpineConfiguration spineConfiguration;

    public String getSampleEndpoint() {
        return prepareRestTemplate()
            .getForObject(spineConfiguration.getSampleEndpoint(), String.class);
    }

    public ResponseEntity sendToACSEndpoint(String message) {
//        return prepareRestTemplate()
//            .postForObject(spineConfiguration.getAcsSetResourcePermissionEndpoint(), message, ResponseEntity.class);
        ResponseEntity<String> responseEntity = prepareRestTemplate()
            .postForEntity(spineConfiguration.getAcsSetResourcePermissionEndpoint(), message, String.class);
        return responseEntity;
    }

    private RestTemplate prepareRestTemplate() {
        var restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(spineConfiguration.getUrl()));
        return restTemplate;
    }
}
