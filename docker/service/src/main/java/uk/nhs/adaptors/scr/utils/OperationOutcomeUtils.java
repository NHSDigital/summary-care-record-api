package uk.nhs.adaptors.scr.utils;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import uk.nhs.adaptors.scr.exceptions.NHSCodings;

public class OperationOutcomeUtils {
    public static OperationOutcome createFromException(Exception exception) {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(OperationOutcome.IssueType.EXCEPTION)
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setDiagnostics(formatException(exception))
            .setDetails(new CodeableConcept().addCoding(NHSCodings.INTERNAL_SERVER_ERROR.asCoding()));
        return operationOutcome;
    }

    private static String formatException(Throwable exception) {
        var sb = new StringBuilder();
        sb.append(exception.getMessage());
        while (exception.getCause() != null) {
            exception = exception.getCause();
            sb.append("; ");
            sb.append(exception.getMessage());
        }
        return sb.toString();
    }
}
