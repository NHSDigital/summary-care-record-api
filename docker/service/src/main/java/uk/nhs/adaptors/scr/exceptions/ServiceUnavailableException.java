package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.TRANSIENT;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

public class ServiceUnavailableException extends ScrBaseException implements OperationOutcomeError {
    public ServiceUnavailableException(String message) {
        super(message);
    }

    @Override
    public OperationOutcome getOperationOutcome() {
        var operationOutcome = new OperationOutcome();

        operationOutcome.addIssue()
                .setSeverity(ERROR)
                .setCode(TRANSIENT)
                .setDetails(new CodeableConcept().setText(getMessage()));

        return operationOutcome;
    }

    @Override
    public HttpStatus getStatusCode() {
        return SERVICE_UNAVAILABLE;
    }
}
