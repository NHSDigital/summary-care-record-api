package uk.nhs.adaptors.scr.config;

import lombok.RequiredArgsConstructor;
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
import uk.nhs.adaptors.scr.clients.spine.SpineStringResponseHandler;
import uk.nhs.adaptors.scr.clients.spine.SpineXmlResponseHandler;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScrSpringConfiguration {

    @Autowired
    private final ScrConfiguration scrConfiguration;
    private final SpineXmlResponseHandler xmlResponseHandler;
    private final SpineStringResponseHandler stringResponseHandler;
    private final SpineHttpClient spineHttpClient;
    private final SpineConfiguration spineConfiguration;
    private final IdentityServiceConfiguration identityServiceConfiguration;

    @Bean
    public SpineClientContract spineClient() {
        if (scrConfiguration.getSandboxMode()) {
            return new SandboxSpineClient();
        } else {
            return new SpineClient(spineConfiguration, spineHttpClient, stringResponseHandler, xmlResponseHandler);
        }
    }

    @Bean
    public IdentityServiceContract identityServiceClient() {
        if (scrConfiguration.getSandboxMode()) {
            return new SandboxIdentityServiceClient();
        } else {
            return new IdentityServiceClient(identityServiceConfiguration, scrConfiguration);
        }
    }
}
