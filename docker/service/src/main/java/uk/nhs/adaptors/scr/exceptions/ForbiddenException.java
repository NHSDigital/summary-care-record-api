package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.FORBIDDEN;

public class ForbiddenException extends ScrBaseException implements OperationOutcomeError {

    private final OperationOutcome operationOutcome;

    public ForbiddenException(String message) {
        super(message);
        operationOutcome = buildOperationOutcome(message);
    }

    @Override
    public OperationOutcome getOperationOutcome() {
        return operationOutcome;
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.FORBIDDEN;
    }

    private static OperationOutcome buildOperationOutcome(String message) {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(FORBIDDEN)
            .setSeverity(ERROR)
            .setDetails(new CodeableConcept().setText(message));
        return operationOutcome;
    }
}
