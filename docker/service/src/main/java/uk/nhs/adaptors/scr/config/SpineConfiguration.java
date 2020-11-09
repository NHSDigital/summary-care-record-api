package uk.nhs.adaptors.scr.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "spine")
@Getter
@Setter
public class SpineConfiguration implements InitializingBean {

    private String url;
    private String acsEndpoint;
    private String scrEndpoint;

    private String endpointPrivateKey;
    private String endpointCert;
    private String caCerts;

    private long scrResultRepeatTimeout;
    private long scrResultTimeout;

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isBlank(endpointPrivateKey)) {
            throw new IllegalArgumentException("endpointPrivateKey is not set");
        }
        if (StringUtils.isBlank(endpointCert)) {
            throw new IllegalArgumentException("endpointCert is not set");
        }
        if (StringUtils.isBlank(caCerts)) {
            throw new IllegalArgumentException("caCerts is not set");
        }
    }
}
