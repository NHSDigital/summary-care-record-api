package uk.nhs.adaptors.scr.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static uk.nhs.adaptors.scr.consts.RequestFiltersOrdering.EXCEPTION_HANDLER_FILTER_ORDER;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class UuidValidationFilterConfig {

    private final UuidValidationFilter uuidValidationFilter;

    @Bean
    public FilterRegistrationBean<UuidValidationFilter> servletRegistrationBeanForCorrelationID() {
        final FilterRegistrationBean<UuidValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(uuidValidationFilter);
        registrationBean.setOrder(EXCEPTION_HANDLER_FILTER_ORDER);
        return registrationBean;
    }
}
