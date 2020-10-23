package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;

public abstract class GatewayTimeoutException extends ScrBaseException implements OperationOutcomeError {
    public GatewayTimeoutException(String message) {
        super(message);
    }

    public GatewayTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public GatewayTimeoutException(Throwable cause) {
        super(cause);
    }

    @Override
    public IBaseOperationOutcome getOperationOutcome() {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setDiagnostics(getMessage());
        return operationOutcome;
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.GATEWAY_TIMEOUT;
    }
}
