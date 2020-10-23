package uk.nhs.adaptors.scr.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParseProcessingResultException extends RuntimeException {
    public ParseProcessingResultException(String message) {
        super(message);
    }
}
