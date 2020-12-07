package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.FATAL;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION;

public abstract class InternalErrorException extends ScrBaseException implements OperationOutcomeError {
    public InternalErrorException(String message) {
        super(message);
    }

    public InternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalErrorException(Throwable cause) {
        super(cause);
    }

    @Override
    public OperationOutcome getOperationOutcome() {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setSeverity(FATAL)
            .setCode(EXCEPTION)
            .setDetails(new CodeableConcept().setText(getMessage()));
        return operationOutcome;
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
