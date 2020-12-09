package uk.nhs.adaptors.sandbox.scr.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class ConversationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "CorrelationId";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
        throws java.io.IOException, ServletException {
        try {
            var token = request.getHeader(CORRELATION_ID_HEADER);
            if (StringUtils.isEmpty(token)) {
                token = getRandomCorrelationId();
            }
            applyCorrelationId(token);
            token = URLEncoder.encode(token, StandardCharsets.UTF_8);
            response.addHeader(CORRELATION_ID_HEADER, token);
            chain.doFilter(request, response);
        } finally {
            resetCorrelationId();
        }
    }

    public String getRandomCorrelationId() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public void applyCorrelationId(String id) {
        MDC.put(MDC_KEY, id);
    }

    public void resetCorrelationId() {
        MDC.remove(MDC_KEY);
    }
}
