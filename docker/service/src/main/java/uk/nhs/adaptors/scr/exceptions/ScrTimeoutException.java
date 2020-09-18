package uk.nhs.adaptors.scr.exceptions;

public class ScrTimeoutException extends ScrBaseException {
    private static final String MESSAGE = "Spine POST + polling GET has timed out";

    public ScrTimeoutException() {
        super(MESSAGE);
    }

    public ScrTimeoutException(Exception e) {
        super(MESSAGE, e);
    }
}
