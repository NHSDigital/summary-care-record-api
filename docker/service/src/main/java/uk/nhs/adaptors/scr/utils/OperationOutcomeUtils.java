package uk.nhs.adaptors.scr.utils;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import uk.nhs.adaptors.scr.exceptions.NHSCodings;

import java.io.PrintWriter;
import java.io.StringWriter;

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

    public static OperationOutcome createFromInternalException(Exception exception) {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(OperationOutcome.IssueType.NOTSUPPORTED)
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setDiagnostics(formatException(exception));
        return operationOutcome;
    }

    private static String formatException(Throwable exception) {
        var errors = new StringWriter();
        exception.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}
