package uk.nhs.adaptors.scr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "spine")
@Getter
@Setter
public class SpineConfiguration {
    private String url;
    private String sampleEndpoint;
}
