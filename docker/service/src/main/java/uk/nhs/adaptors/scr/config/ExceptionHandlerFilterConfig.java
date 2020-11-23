package uk.nhs.adaptors.scr.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class ExceptionHandlerFilterConfig {

    private final ExceptionHandlerFilter exceptionHandlerFilter;

    @Bean
    public FilterRegistrationBean<ExceptionHandlerFilter> servletRegistrationBeanForCorrelationID() {
        final FilterRegistrationBean<ExceptionHandlerFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(exceptionHandlerFilter);
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
