package uk.nhs.adaptors.scr.exceptions;

public class RequestValidationException extends BadRequestException {
    public RequestValidationException(String message) {
        super(message);
    }
}
