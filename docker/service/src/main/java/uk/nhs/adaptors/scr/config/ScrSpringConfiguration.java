package uk.nhs.adaptors.scr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.adaptors.scr.clients.SandboxSpineClient;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.clients.SpineClientContract;
import uk.nhs.adaptors.scr.clients.SpineHttpClient;

@Configuration
public class ScrSpringConfiguration {

    @Autowired
    private ScrConfiguration scrConfiguration;

    @Bean
    @Autowired
    public SpineClientContract spineClient(SpineConfiguration spineConfiguration, SpineHttpClient spineHttpClient) {
        if (scrConfiguration.getSandboxMode()) {
            return new SandboxSpineClient();
        } else {
            return new SpineClient(spineConfiguration, spineHttpClient);
        }

    }
}
