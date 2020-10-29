package uk.nhs.adaptors.scr.exceptions;

import lombok.Getter;

@Getter
public class NoSpineResultException extends GatewayTimeoutException {

    private final long retryAfter;

    public NoSpineResultException(long retryAfter) {
        super("Spine polling yield no result");
        this.retryAfter = retryAfter;
    }
}