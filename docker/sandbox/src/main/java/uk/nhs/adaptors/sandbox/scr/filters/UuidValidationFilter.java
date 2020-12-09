package uk.nhs.adaptors.sandbox.scr.filters;

import ca.uhn.fhir.context.FhirContext;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.nhs.adaptors.sandbox.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.sandbox.scr.filters.consts.HttpHeaders.CORRELATION_ID_HEADER;
import static uk.nhs.adaptors.sandbox.scr.filters.consts.HttpHeaders.REQUEST_ID_LOGGER;

@Component
@AllArgsConstructor
public class UuidValidationFilter extends OncePerRequestFilter {

    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{12}+$";
    private final FhirContext fhirContext = FhirContext.forR4();

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        var correlationIdToken = request.getHeader(CORRELATION_ID_HEADER);
        var requestIdToken = request.getHeader(REQUEST_ID_LOGGER);

        if (!checkValidUUID(correlationIdToken)) {
            throwInvalidUUIDResponse(response, CORRELATION_ID_HEADER);
        } else if (!checkValidUUID(requestIdToken)) {
            throwInvalidUUIDResponse(response, REQUEST_ID_LOGGER);
        } else {
            chain.doFilter(request, response);
        }
    }

    public boolean checkValidUUID(String header) {
        return StringUtils.isEmpty(header) || header.matches(UUID_REGEX);
    }

    public void throwInvalidUUIDResponse(HttpServletResponse response, String headerName) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(APPLICATION_FHIR_JSON_VALUE);

        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(OperationOutcome.IssueType.EXCEPTION)
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setDetails(new CodeableConcept().setText("Invalid " + headerName + ". Should be a UUIDv4 matching \"" + UUID_REGEX + "\""));
        response.getWriter().write(fhirContext.newJsonParser().encodeResourceToString(operationOutcome));
    }
}
