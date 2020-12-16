package uk.nhs.adaptors.scr.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@Configuration
@ConfigurationProperties(prefix = "spine")
@Getter
@Setter
@ToString
public class SpineConfiguration {

    private String url;
    private String acsEndpoint;
    private String scrEndpoint;
    private String psisQueriesEndpoint;

    private String endpointPrivateKey;
    private String endpointCert;
    private String caCerts;
    private String clientKey;
    private String clientCert;
    private String rootCA;
    private String subCA;

    private long scrResultRepeatTimeout;
    @NotNull
    private long scrResultTimeout;
}
