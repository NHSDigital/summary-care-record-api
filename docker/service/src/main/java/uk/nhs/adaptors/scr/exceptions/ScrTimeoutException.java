package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.TRANSIENT;

public class ScrTimeoutException extends GatewayTimeoutException {
    private static final String MESSAGE = "Spine POST + polling GET has timed out";

    public ScrTimeoutException() {
        super(MESSAGE);
    }

    public ScrTimeoutException(Exception e) {
        super(MESSAGE, e);
    }

    @Override
    public OperationOutcome getOperationOutcome() {
        var operationOutcome = new OperationOutcome();

        operationOutcome.addIssue()
            .setSeverity(ERROR)
            .setCode(TRANSIENT)
            .setDetails(new CodeableConcept().setText("Request processing timed out. " + getMessage()));

        return operationOutcome;
    }
}
