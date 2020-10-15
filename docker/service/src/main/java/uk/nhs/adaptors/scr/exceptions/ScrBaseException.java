package uk.nhs.adaptors.scr.exceptions;

public class ScrBaseException extends RuntimeException {

    public ScrBaseException(String message) {
        super(message);
    }

    public ScrBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScrBaseException(Throwable cause) {
        super(cause);
    }
}
