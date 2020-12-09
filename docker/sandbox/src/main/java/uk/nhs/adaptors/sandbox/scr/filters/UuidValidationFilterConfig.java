package uk.nhs.adaptors.sandbox.scr.filters;

import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static uk.nhs.adaptors.sandbox.scr.filters.consts.RequestFiltersOrder.UUID_VALIDATION_FILTER_ORDER;

@Configuration
@AllArgsConstructor
public class UuidValidationFilterConfig {

    private final UuidValidationFilter uuidValidationFilter;

    @Bean
    public FilterRegistrationBean<UuidValidationFilter> servletRegistrationBeanForCorrelationID() {
        final FilterRegistrationBean<UuidValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(uuidValidationFilter);
        registrationBean.setOrder(UUID_VALIDATION_FILTER_ORDER);
        return registrationBean;
    }
}
