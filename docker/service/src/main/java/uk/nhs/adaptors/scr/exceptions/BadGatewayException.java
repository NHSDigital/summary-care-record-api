package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;

public abstract class BadGatewayException extends ScrBaseException implements OperationOutcomeError {
    public BadGatewayException(String message) {
        super(message);
    }

    public BadGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadGatewayException(Throwable cause) {
        super(cause);
    }

    @Override
    public OperationOutcome getOperationOutcome() {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setDiagnostics(getMessage());
        return operationOutcome;
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.BAD_GATEWAY;
    }
}