package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;

public abstract class BadRequestException extends ScrBaseException implements OperationOutcomeError {

    private final OperationOutcome operationOutcome;

    public BadRequestException(String message) {
        super(message);
        operationOutcome = buildOperationOutcome(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        operationOutcome = buildOperationOutcome(message);
    }

    private static OperationOutcome buildOperationOutcome(String message) {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(OperationOutcome.IssueType.VALUE)
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setDiagnostics(message)
            .setDetails(new CodeableConcept().addCoding(NHSCodings.BAD_REQUEST.asCoding()));
        return operationOutcome;
    }

    @Override
    public final OperationOutcome getOperationOutcome() {
        return operationOutcome;
    }

    @Override
    public final HttpStatus getStatusCode() {
        return HttpStatus.BAD_REQUEST;
    }
}
