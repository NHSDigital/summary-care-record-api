package uk.nhs.adaptors.scr.exceptions;

public class UnexpectedSdsResponseException extends InternalErrorException {

    public UnexpectedSdsResponseException(String message) {
        super(message);
    }

    public UnexpectedSdsResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
