package uk.nhs.adaptors.sandbox.scr.handlers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTSUPPORTED;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.VALUE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.nhs.adaptors.sandbox.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@ControllerAdvice
@RestController
@Slf4j
public class OperationOutcomeExceptionHandler extends ResponseEntityExceptionHandler {

    private IParser jsonParser = FhirContext.forR4().newJsonParser();

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
        String errorMessage = servletReq.getRequestURI() + " not found";
        OperationOutcome operationOutcome = createOperationOutcome(NOTFOUND, ERROR, errorMessage);

        return errorResponse(ex, requestHeaders, status, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        OperationOutcome operationOutcome = createOperationOutcome(NOTSUPPORTED, ERROR, ex.getMessage());
        return errorResponse(ex, requestHeaders, status, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        OperationOutcome operationOutcome = createOperationOutcome(VALUE, ERROR, ex.getMessage());
        return errorResponse(ex, requestHeaders, status, operationOutcome);
    }

    @SneakyThrows
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        OperationOutcome operationOutcome = createOperationOutcome(VALUE, ERROR, ex.getMessage());
        return errorResponse(ex, new HttpHeaders(), BAD_REQUEST, operationOutcome);
    }

    private ResponseEntity<Object> errorResponse(Exception ex, HttpHeaders headers, HttpStatus status,
                                                 OperationOutcome operationOutcome) {
        LOGGER.error("Creating OperationOutcome response for " + ex.getClass(), ex);
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_FHIR_JSON_VALUE));
        String content = jsonParser.setPrettyPrint(true).encodeResourceToString(operationOutcome);
        return new ResponseEntity<>(content, headers, status);
    }

    private static OperationOutcome createOperationOutcome(IssueType type, IssueSeverity severity, String message) {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(type)
            .setSeverity(severity)
            .setDetails(new CodeableConcept().setText(message));
        return operationOutcome;
    }
}
