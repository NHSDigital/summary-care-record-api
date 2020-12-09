package uk.nhs.adaptors.sandbox.scr.handlers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTSUPPORTED;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.nhs.adaptors.sandbox.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@ControllerAdvice
@RestController
@Slf4j
public class OperationOutcomeExceptionHandler extends ResponseEntityExceptionHandler {

    private IParser jsonParser = FhirContext.forR4().newJsonParser();

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        LOGGER.error("Creating OperationOutcome response for " + ex.getClass(), ex);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_FHIR_JSON_VALUE));

        OperationOutcome operationOutcome = new OperationOutcome();
        HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
        operationOutcome.addIssue()
            .setCode(NOTFOUND)
            .setSeverity(ERROR)
            .setDetails(new CodeableConcept().setText(servletReq.getRequestURI() + " not found"));

        return new ResponseEntity<>(jsonParser.encodeResourceToString(operationOutcome), headers, NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        LOGGER.error("Creating OperationOutcome response for " + ex.getClass(), ex);
        String contentType = request.getHeader(CONTENT_TYPE);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_FHIR_JSON_VALUE));

        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(NOTSUPPORTED)
            .setSeverity(ERROR)
            .setDetails(new CodeableConcept().setText(contentType + "Content-Type not supported"));

        return new ResponseEntity<>(jsonParser.encodeResourceToString(operationOutcome), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.error("Creating OperationOutcome response for unhandled exception", ex);
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_FHIR_JSON_VALUE));
        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(EXCEPTION)
            .setSeverity(ERROR)
            .setDetails(new CodeableConcept().setText("Internal server error: " + ex.getMessage()));

        String content = jsonParser.encodeResourceToString(operationOutcome);
        return new ResponseEntity<>(content, headers, status);
    }
}
