package uk.nhs.adaptors.scr.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static uk.nhs.adaptors.scr.consts.RequestFiltersOrdering.CONVERSATION_ID_FILTER_ORDER;

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
