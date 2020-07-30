package uk.nhs.adaptors.scr.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.nhs.adaptors.scr.config.SpineConfiguration;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpineClient {
    private final SpineConfiguration spineConfiguration;

    public String getSampleEndpoint() {
        return new RestTemplate()
            .getForObject(spineConfiguration.getSampleEndpoint(), String.class);
    }
}
