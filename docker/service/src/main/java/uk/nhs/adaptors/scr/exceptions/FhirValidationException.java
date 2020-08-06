package uk.nhs.adaptors.scr.exceptions;

public class FhirValidationException extends BadRequestException {
    public FhirValidationException(String message) {
        super(message);
    }
}
