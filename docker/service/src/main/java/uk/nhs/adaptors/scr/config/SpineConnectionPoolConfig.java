package uk.nhs.adaptors.scr.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "spine.connection-pool")
@Getter
@Setter
public class SpineConnectionPoolConfig {
    private int maxTotalConnections;
    private int defaultKeepAliveTime;
    private int connectionTimeout;
    private int requestTimeout;
    private int socketTimeout;
    private int idleConnectionWaitTime;
}
