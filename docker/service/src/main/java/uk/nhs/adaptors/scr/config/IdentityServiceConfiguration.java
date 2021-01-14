package uk.nhs.adaptors.scr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "identity-service")
@Getter
@Setter
public class IdentityServiceConfiguration {
    private String userInfoEndpoint;
}
