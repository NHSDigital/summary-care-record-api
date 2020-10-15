package uk.nhs.adaptors.scr.exceptions;

import lombok.Getter;

@Getter
public class SoapClientException extends Exception {
    private static final String REASON_TEMPLATE = "'%s' not found.";
    private static final String MESSAGE_TEMPLATE = "'%s' missing from SOAP %s, this is a required field.";
    private final String reason;

    public SoapClientException(String reason, String errorLocation) {
        super(String.format(MESSAGE_TEMPLATE, reason, errorLocation));
        this.reason = String.format(REASON_TEMPLATE, reason);
    }
}
