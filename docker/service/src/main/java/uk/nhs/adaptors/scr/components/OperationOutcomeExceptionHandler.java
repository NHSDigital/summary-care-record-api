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
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.nhs.adaptors.scr.exceptions.OperationOutcomeError;
import uk.nhs.adaptors.scr.exceptions.ScrGatewayTimeoutException;
import uk.nhs.adaptors.scr.utils.OperationOutcomeUtils;

import java.util.List;

@ControllerAdvice
@RestController
@Slf4j
public class OperationOutcomeExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private FhirParser fhirParser;

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<String> handleAllErrors(Exception ex, WebRequest request) throws HttpMediaTypeNotAcceptableException {
        LOGGER.error("Creating OperationOutcome response for unhandled exception", ex);

        MediaType mediaType = getRequestMediaType(request);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, List.of(mediaType.toString()));

        if (ex instanceof OperationOutcomeError) {
            OperationOutcomeError error = (OperationOutcomeError) ex;
            String content = fhirParser.encodeResource(mediaType, error.getOperationOutcome());
            return new ResponseEntity<>(content, headers, error.getStatusCode());
        }
        var statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof ScrGatewayTimeoutException) {
            statusCode = HttpStatus.GATEWAY_TIMEOUT;
        }
        OperationOutcome operationOutcome = OperationOutcomeUtils.createFromMessage(ex.getMessage());
        String content = fhirParser.encodeResource(mediaType, operationOutcome);
        return new ResponseEntity<>(content, headers, statusCode);
    }

    private MediaType getRequestMediaType(WebRequest request) {
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            throw new IllegalStateException("Request content type is null");
        }
        return MediaType.parseMediaType(contentType);
    }
}
