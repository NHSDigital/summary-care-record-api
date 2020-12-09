package uk.nhs.adaptors.sandbox.scr.filters;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static uk.nhs.adaptors.sandbox.scr.filters.consts.RequestFiltersOrder.CONVERSATION_ID_FILTER_ORDER;

@Configuration
public class ConversationIdFilterConfig {

    @Bean
    public FilterRegistrationBean<ConversationIdFilter> servletRegistrationBean() {
        final FilterRegistrationBean<ConversationIdFilter> registrationBean = new FilterRegistrationBean<>();
        final ConversationIdFilter conversationIdFilter = new ConversationIdFilter();
        registrationBean.setFilter(conversationIdFilter);
        registrationBean.setOrder(CONVERSATION_ID_FILTER_ORDER);
        return registrationBean;
    }
}
