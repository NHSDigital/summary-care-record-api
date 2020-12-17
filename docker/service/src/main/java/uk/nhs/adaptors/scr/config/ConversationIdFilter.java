package uk.nhs.adaptors.scr.config;

import static uk.nhs.adaptors.scr.consts.HttpHeaders.CORRELATION_ID;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class ConversationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_MDC_KEY = "CorrelationId";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
        throws java.io.IOException, ServletException {
        try {
            var token = request.getHeader(CORRELATION_ID);
            if (StringUtils.isEmpty(token)) {
                token = getRandomCorrelationId();
            }
            applyCorrelationId(token);
            token = URLEncoder.encode(token, StandardCharsets.UTF_8);
            response.addHeader(CORRELATION_ID, token);
            chain.doFilter(request, response);
        } finally {
            resetCorrelationId();
        }
    }

    public String getRandomCorrelationId() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public void applyCorrelationId(String id) {
        MDC.put(CORRELATION_ID_MDC_KEY, id);
    }

    public void resetCorrelationId() {
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }
}
