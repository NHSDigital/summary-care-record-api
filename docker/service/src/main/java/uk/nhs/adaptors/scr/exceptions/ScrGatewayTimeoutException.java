package uk.nhs.adaptors.scr.exceptions;

public class ScrGatewayTimeoutException extends ScrBaseException {
    public ScrGatewayTimeoutException(String message) {
        super(message);
    }

    public ScrGatewayTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScrGatewayTimeoutException(Throwable cause) {
        super(cause);
    }
}
