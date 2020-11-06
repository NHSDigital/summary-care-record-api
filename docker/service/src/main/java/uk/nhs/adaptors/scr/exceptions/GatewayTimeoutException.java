package uk.nhs.adaptors.scr.exceptions;

import org.springframework.http.HttpStatus;

public abstract class GatewayTimeoutException extends ScrBaseException implements OperationOutcomeError {
    public GatewayTimeoutException(String message) {
        super(message);
    }

    public GatewayTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public GatewayTimeoutException(Throwable cause) {
        super(cause);
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.GATEWAY_TIMEOUT;
    }
}
