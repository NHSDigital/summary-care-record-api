package uk.nhs.adaptors.scr.components;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.nhs.adaptors.scr.exceptions.OperationOutcomeError;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTSUPPORTED;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.VALUE;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.scr.utils.OperationOutcomeUtils.createOperationOutcome;

@ControllerAdvice
@RestController
@Slf4j
public class OperationOutcomeExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Map<String, List<HttpMethod>> ALLOWED_METHODS = Map.of("/Bundle", List.of(POST));

    @Autowired
    private FhirParser fhirParser;

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
        String errorMessage = servletReq.getRequestURI() + " not found";
        OperationOutcome operationOutcome = createOperationOutcome(NOTFOUND, ERROR, errorMessage);

        return errorResponse(ex, requestHeaders, status, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.put(ALLOW, List.of(getAllowedMethods(request)));
        OperationOutcome operationOutcome = createOperationOutcome(NOTSUPPORTED, ERROR, ex.getMessage());
        return errorResponse(ex, headers, status, operationOutcome);
    }

    private String getAllowedMethods(WebRequest request) {
        HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
        String requestURI = servletReq.getRequestURI();
        return ALLOWED_METHODS.get(requestURI)
            .stream()
            .map(it -> it.name())
            .collect(joining(","));
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        OperationOutcome operationOutcome;
        HttpStatus httpStatus;
        if (ex instanceof OperationOutcomeError) {
            OperationOutcomeError error = (OperationOutcomeError) ex;
            operationOutcome = error.getOperationOutcome();
            httpStatus = error.getStatusCode();
        } else {
            operationOutcome = createOperationOutcome(EXCEPTION, ERROR, ex.getMessage());
            httpStatus = INTERNAL_SERVER_ERROR;
        }

        return errorResponse(ex, new HttpHeaders(), httpStatus, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleAllExceptions(ex);
    }

    private ResponseEntity<Object> errorResponse(Exception ex, HttpHeaders headers, HttpStatus status,
                                                 OperationOutcome operationOutcome) {
        LOGGER.error("Creating OperationOutcome response for " + ex.getClass(), ex);
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_FHIR_JSON_VALUE));
        String content = fhirParser.encodeToJson(operationOutcome);
        return new ResponseEntity<>(content, headers, status);
    }
}
