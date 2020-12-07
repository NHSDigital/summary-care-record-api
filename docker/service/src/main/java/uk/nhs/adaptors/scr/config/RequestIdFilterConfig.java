package uk.nhs.adaptors.scr.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static uk.nhs.adaptors.scr.consts.RequestFiltersOrdering.REQUEST_ID_FILTER_ORDER;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class RequestIdFilterConfig {

    private final RequestIdFilter requestIdFilter;

    @Bean
    public FilterRegistrationBean<RequestIdFilter> servletRegistrationBeanForRequestID() {
        final FilterRegistrationBean<RequestIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestIdFilter);
        registrationBean.setOrder(REQUEST_ID_FILTER_ORDER);
        return registrationBean;
    }
}
