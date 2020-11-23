package uk.nhs.adaptors.scr.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class LoggingIdFilterConfig {

    private final LoggingIdFilter loggingIdFilter;

    @Bean
    public FilterRegistrationBean<LoggingIdFilter> servletRegistrationBeanForLoggingID() {
        final FilterRegistrationBean<LoggingIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(loggingIdFilter);
        registrationBean.setOrder(3);
        return registrationBean;
    }
}
