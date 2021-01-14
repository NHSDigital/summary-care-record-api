package uk.nhs.adaptors.scr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.adaptors.scr.clients.identity.IdentityServiceClient;
import uk.nhs.adaptors.scr.clients.identity.IdentityServiceContract;
import uk.nhs.adaptors.scr.clients.identity.SandboxIdentityServiceClient;
import uk.nhs.adaptors.scr.clients.spine.SandboxSpineClient;
import uk.nhs.adaptors.scr.clients.spine.SpineClient;
import uk.nhs.adaptors.scr.clients.spine.SpineClientContract;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient;

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

    @Bean
    @Autowired
    public IdentityServiceContract identityServiceClient(IdentityServiceConfiguration configuration) {
        if (scrConfiguration.getSandboxMode()) {
            return new SandboxIdentityServiceClient();
        } else {
            return new IdentityServiceClient(configuration);
        }
    }
}
