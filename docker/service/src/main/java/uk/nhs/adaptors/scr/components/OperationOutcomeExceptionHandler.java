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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.nhs.adaptors.scr.exceptions.OperationOutcomeError;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTSUPPORTED;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.VALUE;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.scr.utils.OperationOutcomeUtils.createOperationOutcome;

@ControllerAdvice
@RestController
@Slf4j
public class OperationOutcomeExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Map<String, List<HttpMethod>> ALLOWED_METHODS = Map.of(
        "/Bundle", List.of(GET, POST),
        "/DocumentReference", List.of(GET),
        "/$setPermission", List.of(POST),
        "/healthcheck", List.of(GET)
    );

    @Autowired
    private FhirParser fhirParser;

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
        String errorMessage = servletReq.getRequestURI() + " not found";
        OperationOutcome operationOutcome = createOperationOutcome(NOTFOUND, ERROR, errorMessage);

        return errorResponse(requestHeaders, status, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.put(ALLOW, List.of(getAllowedMethods(request)));
        OperationOutcome operationOutcome = createOperationOutcome(NOTSUPPORTED, ERROR, ex.getMessage());
        return errorResponse(headers, status, operationOutcome);
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
        return errorResponse(requestHeaders, status, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        OperationOutcome operationOutcome = createOperationOutcome(VALUE, ERROR, ex.getMessage());
        return errorResponse(requestHeaders, status, operationOutcome);
    }

    @SneakyThrows
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingRequestHeader(Exception ex) {
        OperationOutcome operationOutcome = createOperationOutcome(VALUE, ERROR, ex.getMessage());
        return errorResponse(new HttpHeaders(), BAD_REQUEST, operationOutcome);
    }

    @SneakyThrows
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        OperationOutcome operationOutcome = createOperationOutcome(VALUE, ERROR, removeMethodNamePrefix(ex.getMessage()));
        return errorResponse(new HttpHeaders(), BAD_REQUEST, operationOutcome);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String responseMessage = String.format("Invalid value - %s in field '%s'", ex.getValue(), ex.getName());
        OperationOutcome operationOutcome = createOperationOutcome(VALUE, ERROR, responseMessage);
        return errorResponse(new HttpHeaders(), BAD_REQUEST, operationOutcome);
    }

    private String removeMethodNamePrefix(String message) {
        if (isNotEmpty(message) && message.contains(":")) {
            return message.substring(message.indexOf(":") + 1).trim();
        }
        return message;
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

        return errorResponse(new HttpHeaders(), httpStatus, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleAllExceptions(ex);
    }

    private ResponseEntity<Object> errorResponse(HttpHeaders headers, HttpStatus status,
                                                 OperationOutcome operationOutcome) {
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_FHIR_JSON_VALUE));
        String content = fhirParser.encodeToJson(operationOutcome);
        return new ResponseEntity<>(content, headers, status);
    }
}
