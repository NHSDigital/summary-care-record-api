package uk.nhs.adaptors.scr.exceptions;

import lombok.Getter;

@Getter
public class NoScrResultException extends ScrBaseException {

    private final long retryAfter;

    public NoScrResultException(long retryAfter) {
        super("SCR polling yield no result");
        this.retryAfter = retryAfter;
    }
}