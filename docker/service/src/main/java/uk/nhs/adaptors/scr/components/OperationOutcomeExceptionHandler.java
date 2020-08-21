package uk.nhs.adaptors.scr.components;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.nhs.adaptors.scr.controllers.fhir.FhirController;
import uk.nhs.adaptors.scr.exceptions.OperationOutcomeError;
import uk.nhs.adaptors.scr.utils.OperationOutcomeUtils;

import java.util.List;

import static java.util.Collections.singletonList;

@ControllerAdvice(basePackageClasses = FhirController.class)
@RestController
@Slf4j
public class OperationOutcomeExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ANY_MEDIA_TYPE = "*/*";

    @Autowired
    private FhirParser fhirParser;

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<String> handleAllErrors(Exception ex, WebRequest request) {
        LOGGER.error("Creating OperationOutcome response for unhandled exception", ex);

        MediaType mediaType = getTargetMediaType(request);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, List.of(mediaType.toString()));

        if (ex instanceof OperationOutcomeError) {
            OperationOutcomeError error = (OperationOutcomeError) ex;
            String content = fhirParser.encodeResource(mediaType, error.getOperationOutcome());
            return new ResponseEntity<>(content, headers, error.getStatusCode());
        }
        OperationOutcome operationOutcome = OperationOutcomeUtils.createFromMessage(ex.getMessage());
        String content = fhirParser.encodeResource(mediaType, operationOutcome);
        return new ResponseEntity<>(content, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        LOGGER.error("Creating OperationOutcome response for unhandled exception", ex);

        MediaType mediaType = getTargetMediaType(request);

        headers.put(HttpHeaders.CONTENT_TYPE, singletonList(mediaType.toString()));
        OperationOutcome operationOutcome = OperationOutcomeUtils.createFromMessage(ex.getMessage());
        String content = fhirParser.encodeResource(mediaType, operationOutcome);
        return new ResponseEntity<>(content, headers, status);
    }

    private MediaType getTargetMediaType(WebRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        String targetContentType;
        if (accept == null || ANY_MEDIA_TYPE.equals(accept)) {
            targetContentType = contentType;
        } else {
            targetContentType = accept;
        }

        if (targetContentType == null) {
            throw new IllegalStateException("Request accept and content type are null");
        }

        return MediaType.parseMediaType(targetContentType);
    }
}
