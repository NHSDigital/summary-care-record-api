package uk.nhs.adaptors.scr.config;

import static uk.nhs.adaptors.scr.consts.HttpHeaders.LOGGING_ID_HEADER;

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
public class LoggingIdFilter extends OncePerRequestFilter {

    public static final String MDC_KEY = "RequestId";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
        throws java.io.IOException, ServletException {
        try{
            var token = request.getHeader(LOGGING_ID_HEADER);
            if (StringUtils.isEmpty(token)) {
                token = getRandomRequestId();
            }
            applyCorrelationId(token);
            token = URLEncoder.encode(token, StandardCharsets.UTF_8);
            response.addHeader(LOGGING_ID_HEADER, token);
            chain.doFilter(request, response);
        }finally {
            resetRequestId();
        }
    }

    public void applyCorrelationId(String id) {
        MDC.put(MDC_KEY, id);
    }

    public String getRandomRequestId() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public void resetRequestId() {
        MDC.remove(MDC_KEY);
    }

}
