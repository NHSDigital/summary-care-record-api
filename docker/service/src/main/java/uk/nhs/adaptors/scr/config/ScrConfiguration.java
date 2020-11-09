package uk.nhs.adaptors.scr.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scr")
@Getter
@Setter
public class ScrConfiguration implements InitializingBean {
    private String partyIdTo;
    private String partyIdFrom;
    private String nhsdAsidTo;

    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isBlank(partyIdTo)) {
            throw new IllegalArgumentException("partyIdTo is not set");
        }
        if (StringUtils.isBlank(partyIdFrom)) {
            throw new IllegalArgumentException("partyIdFrom is not set");
        }
        if (StringUtils.isBlank(nhsdAsidTo)) {
            throw new IllegalArgumentException("nhsdAsidTo is not set");
        }
    }
}
