package uk.nhs.adaptors.scr.exceptions;

public class UnexpectedSpineResponseException extends InternalErrorException {

    public UnexpectedSpineResponseException(String message) {
        super(message);
    }

    public UnexpectedSpineResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
