package uk.nhs.adaptors.scr.utils;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;

public class OperationOutcomeUtils {
    public static OperationOutcome createOperationOutcome(IssueType type, IssueSeverity severity, String message) {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(type)
            .setSeverity(severity)
            .setDetails(new CodeableConcept().setText(message));
        return operationOutcome;
    }
}
