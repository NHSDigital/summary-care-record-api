package uk.nhs.adaptors.scr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scr")
@Getter
@Setter
public class ScrConfiguration {
    private String partyIdTo;
    private String partyIdFrom;
    private String nhsdAsidTo;
    private Boolean sandboxMode;
}
