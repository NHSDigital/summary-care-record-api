package uk.nhs.adaptors.scr.config;

import static uk.nhs.adaptors.scr.consts.HttpHeaders.CORRELATION_ID_HEADER;
import static uk.nhs.adaptors.scr.consts.HttpHeaders.LOGGING_ID_HEADER;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.NHSCodings;

@Component
@AllArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{4}+-[0-9a-fA-F]{12}+$";
    private final FhirParser fhirParser;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        var correlation_id = request.getHeader(CORRELATION_ID_HEADER);
        var logging_id = request.getHeader(LOGGING_ID_HEADER);

        if (StringUtils.isNotEmpty(correlation_id) && !correlation_id.matches(UUID_REGEX)) {
            invalidResponse(response, CORRELATION_ID_HEADER);
        } else if(StringUtils.isNotEmpty(logging_id) && !logging_id.matches(UUID_REGEX)) {
            invalidResponse(response, LOGGING_ID_HEADER);
        } else {
            chain.doFilter(request, response);
        }
    }

    public void invalidResponse(HttpServletResponse response, String header_name)
        throws ServletException, IOException{
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(APPLICATION_FHIR_JSON_VALUE);

        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(OperationOutcome.IssueType.EXCEPTION)
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setDiagnostics("Invalid " + header_name + ". Should be a UUIDv4 matching \"" + UUID_REGEX + "\"")
            .setDetails(new CodeableConcept().addCoding(NHSCodings.BAD_REQUEST.asCoding()));

        response.getWriter().write(fhirParser.encodeResource(APPLICATION_FHIR_JSON, operationOutcome));
    }
}
