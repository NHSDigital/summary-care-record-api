package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;
import uk.nhs.adaptors.scr.utils.OperationOutcomeUtils;

public abstract class BadRequestException extends ScrBaseException implements OperationOutcomeError {

    private final IBaseOperationOutcome operationOutcome;

    public BadRequestException(String message) {
        super(message);
        operationOutcome = OperationOutcomeUtils.createFromMessage(message, getIssueType());
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        operationOutcome = OperationOutcomeUtils.createFromMessage(message, getIssueType());
    }

    public BadRequestException(Throwable cause) {
        super(cause);
        operationOutcome = OperationOutcomeUtils.createFromMessage(cause.getMessage(), getIssueType());
    }

    @Override
    final public IBaseOperationOutcome getOperationOutcome() {
        return operationOutcome;
    }

    public OperationOutcome.IssueType getIssueType() {
        return OperationOutcome.IssueType.INVALID;
    }

    @Override
    final public HttpStatus getStatusCode() {
        return HttpStatus.BAD_REQUEST;
    }
}
