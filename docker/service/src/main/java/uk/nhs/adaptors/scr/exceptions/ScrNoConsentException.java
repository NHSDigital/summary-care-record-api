package uk.nhs.adaptors.scr.exceptions;

public class ScrNoConsentException extends BadRequestException {
    public ScrNoConsentException(String reason) {
        super(reason);
    }
}
