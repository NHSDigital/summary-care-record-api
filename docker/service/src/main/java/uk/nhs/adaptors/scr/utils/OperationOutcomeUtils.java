package uk.nhs.adaptors.scr.utils;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;

public class OperationOutcomeUtils {
    public static OperationOutcome createFromMessage(String message) {
        return createFromMessage(message, OperationOutcome.IssueType.UNKNOWN);
    }

    public static OperationOutcome createFromMessage(String message, OperationOutcome.IssueType code) {
        OperationOutcome operationOutcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue = operationOutcome.addIssue();
        issue.setCode(code);
        issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
        CodeableConcept details = new CodeableConcept();
        details.setText(message);
        issue.setDetails(details);
        return operationOutcome;
    }
}
