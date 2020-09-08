package uk.nhs.adaptors.scr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spine")
@Getter
@Setter
public class SpineConfiguration {
    private String url;
    private String acsEndpoint;
    private String scrEndpoint;

    private String endpointPrivateKey;
    private String endpointCert;
    private String caCerts;

    private long scrResultRepeatBackoff;
    private long scrResultRepeatTimeout;
    private long scrResultHardTimeout;
    private long scrResultTimeout;
}
