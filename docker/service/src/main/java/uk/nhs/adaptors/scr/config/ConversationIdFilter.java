package uk.nhs.adaptors.scr.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;

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

    private static final String HEADER_NAME = "X-Correlation-ID";
    private static final String MDC_KEY = "CorrelationId";
    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{12}+$";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
        throws java.io.IOException, ServletException {
        try {
            var token = request.getHeader(HEADER_NAME);
            if (StringUtils.isEmpty(token)) {
                token = getRandomCorrelationId();
            }
            if (!token.matches(UUID_REGEX)) {
                response.sendError(500, " is not valid UUID");
                response.setStatus(404);
                response.getWriter().write("this is my error");
            }
            else {
                applyCorrelationId(token);
                token = URLEncoder.encode(token, StandardCharsets.UTF_8);
                response.addHeader(HEADER_NAME, token);
                chain.doFilter(request, response);
            }

        } finally {
            resetCorrelationId();
        }
    }

    public void applyCorrelationId(String id) {
        MDC.put(MDC_KEY, id);
    }

    public String getRandomCorrelationId() {
        return UUID.randomUUID().toString().toUpperCase().replace("-", "");
    }

    public void resetCorrelationId() {
        MDC.remove(MDC_KEY);
    }
}
