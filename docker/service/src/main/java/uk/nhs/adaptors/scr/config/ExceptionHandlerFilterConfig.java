package uk.nhs.adaptors.scr.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static uk.nhs.adaptors.scr.consts.RequestFiltersOrdering.EXCEPTION_HANDLER_ORDER_POS;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class ExceptionHandlerFilterConfig {

    private final ExceptionHandlerFilter exceptionHandlerFilter;

    @Bean
    public FilterRegistrationBean<ExceptionHandlerFilter> servletRegistrationBeanForCorrelationID() {
        final FilterRegistrationBean<ExceptionHandlerFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(exceptionHandlerFilter);
        registrationBean.setOrder(EXCEPTION_HANDLER_ORDER_POS);
        return registrationBean;
    }
}
