package uk.nhs.adaptors.scr.exceptions;

public class UnexpectedSpineResponseException extends BadGatewayException {

    public UnexpectedSpineResponseException(String message) {
        super(message);
    }

    public UnexpectedSpineResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
