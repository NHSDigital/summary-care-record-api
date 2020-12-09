package uk.nhs.adaptors.scr.components;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.nhs.adaptors.scr.exceptions.OperationOutcomeError;
import uk.nhs.adaptors.scr.utils.OperationOutcomeUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@ControllerAdvice
@RestController
@Slf4j
public class OperationOutcomeExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private FhirParser fhirParser;

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        LOGGER.error("Creating OperationOutcome response for NoHandlerFoundException", ex);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(CONTENT_TYPE, List.of(APPLICATION_FHIR_JSON_VALUE));

        OperationOutcome operationOutcome = new OperationOutcome();
        HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
        operationOutcome.addIssue()
            .setCode(NOTFOUND)
            .setSeverity(ERROR)
            .setDetails(new CodeableConcept().setText(servletReq.getRequestURI() + " not found"));

        return new ResponseEntity<>(fhirParser.encodeToJson(operationOutcome), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        LOGGER.error("Creating OperationOutcome response for HttpMediaTypeNotSupportedException", ex);
        String contentType = request.getHeader(CONTENT_TYPE);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(CONTENT_TYPE, List.of(APPLICATION_FHIR_JSON_VALUE));

        OperationOutcome operationOutcome = OperationOutcomeUtils.createFromMediaTypeNotSupportedException(ex);

        return new ResponseEntity<>(fhirParser.encodeToJson(operationOutcome), headers, status);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<String> handleAllErrors(Exception ex, WebRequest request)
        throws HttpMediaTypeNotAcceptableException {
        LOGGER.error("Creating OperationOutcome response for unhandled exception", ex);

        MediaType mediaType = getRequestMediaType(request);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(CONTENT_TYPE, List.of(mediaType.toString()));

        OperationOutcome operationOutcome;
        HttpStatus httpStatus;
        if (ex instanceof OperationOutcomeError) {
            OperationOutcomeError error = (OperationOutcomeError) ex;
            operationOutcome = error.getOperationOutcome();
            httpStatus = error.getStatusCode();
        } else {
            operationOutcome = OperationOutcomeUtils.createFromException(ex);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String content = fhirParser.encodeResource(mediaType, operationOutcome);

        return new ResponseEntity<>(content, headers, httpStatus);
    }

    @SneakyThrows
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

        LOGGER.error("Creating OperationOutcome response for unhandled exception", ex);
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_FHIR_JSON_VALUE));
        OperationOutcome operationOutcome = OperationOutcomeUtils.createFromException(ex);

        String content = fhirParser.encodeResource(APPLICATION_FHIR_JSON, operationOutcome);
        return new ResponseEntity<>(content, headers, status);
    }

    private MediaType getRequestMediaType(WebRequest request) {
        String contentType = request.getHeader(CONTENT_TYPE);
        if (contentType == null) {
            throw new IllegalStateException("Request content type is null");
        }
        return MediaType.parseMediaType(contentType);
    }
}
