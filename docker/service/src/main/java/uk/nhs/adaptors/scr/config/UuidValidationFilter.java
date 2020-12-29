package uk.nhs.adaptors.scr.config;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.nhs.adaptors.scr.components.FhirParser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.VALUE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CORRELATION_ID;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.REQUEST_ID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@Component
@AllArgsConstructor
public class UuidValidationFilter extends OncePerRequestFilter {

    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{12}+$";
    private final FhirParser fhirParser;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        var correlationIdToken = request.getHeader(CORRELATION_ID);
        var requestIdToken = request.getHeader(REQUEST_ID);

        if (!checkValidUUID(correlationIdToken)) {
            throwInvalidUUIDResponse(response, CORRELATION_ID);
        } else if (!checkValidUUID(requestIdToken)) {
            throwInvalidUUIDResponse(response, REQUEST_ID);
        } else {
            chain.doFilter(request, response);
        }
    }

    public boolean checkValidUUID(String header) {
        return StringUtils.isEmpty(header) || header.matches(UUID_REGEX);
    }

    public void throwInvalidUUIDResponse(HttpServletResponse response, String headerName)
        throws IOException {
        response.setStatus(BAD_REQUEST.value());
        response.setContentType(APPLICATION_FHIR_JSON_VALUE);

        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(VALUE)
            .setSeverity(ERROR)
            .setDetails(new CodeableConcept()
                .setText("Invalid " + headerName + ". Should be a UUIDv4 matching \"" + UUID_REGEX + "\""));

        response.getWriter().write(fhirParser.encodeToJson(operationOutcome));
    }
}
