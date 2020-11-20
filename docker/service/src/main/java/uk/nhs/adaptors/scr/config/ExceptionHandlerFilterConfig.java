package uk.nhs.adaptors.scr.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExceptionHandlerFilterConfig {

    @Bean
    public FilterRegistrationBean<ExceptionHandlerFilter> servletRegistrationBean2() {
        final FilterRegistrationBean<ExceptionHandlerFilter> registrationBean = new FilterRegistrationBean<>();
        final ExceptionHandlerFilter exceptionHandlerFilter = new ExceptionHandlerFilter();
        registrationBean.setFilter(exceptionHandlerFilter);
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
