package uk.nhs.adaptors.scr.exceptions;

public class FhirMappingException extends BadRequestException {
    public FhirMappingException(String message) {
        super(message);
    }

    public FhirMappingException(Throwable cause) {
        super("Mapping exception", cause);
    }

    public FhirMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
