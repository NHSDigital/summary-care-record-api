package uk.nhs.adaptors.scr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.adaptors.scr.clients.MockSpineClient;
import uk.nhs.adaptors.scr.clients.ProdSpineClient;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.clients.SpineHttpClient;

@Configuration
public class ScrSpringConfiguration {

    @Autowired
    private ScrConfiguration scrConfiguration;

    @Bean
    @Autowired
    public SpineClient spineClient(SpineConfiguration spineConfiguration, SpineHttpClient spineHttpClient) {
        switch (scrConfiguration.getSpineMode()) {
            case REAL:
                return new ProdSpineClient(spineConfiguration, spineHttpClient);
            case MOCK:
                return new MockSpineClient();
            default:
                throw new IllegalStateException("Invalid scr.spineMode variable value: " + scrConfiguration.getSpineMode());
        }

    }
}
