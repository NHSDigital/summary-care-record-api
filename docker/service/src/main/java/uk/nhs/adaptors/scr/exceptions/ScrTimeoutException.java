package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;

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
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setCode(OperationOutcome.IssueType.TRANSIENT)
            .setDetails(new CodeableConcept().addCoding(NHSCodings.NO_RECORD_FOUND.asCoding()))
            .setDiagnostics("Request processing timed out. " + getMessage());

        return operationOutcome;
    }
}
