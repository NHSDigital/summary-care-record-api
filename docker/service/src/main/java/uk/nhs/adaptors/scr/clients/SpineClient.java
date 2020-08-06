package uk.nhs.adaptors.scr.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    private RestTemplate prepareRestTemplate() {
        var restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(spineConfiguration.getUrl()));
        return restTemplate;
    }
}
