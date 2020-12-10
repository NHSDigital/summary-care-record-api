package uk.nhs.adaptors.scr.utils;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import uk.nhs.adaptors.scr.exceptions.NHSCodings;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTSUPPORTED;

public class OperationOutcomeUtils {
    public static OperationOutcome createFromException(Exception exception) {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(EXCEPTION)
            .setSeverity(ERROR)
            .setDetails(new CodeableConcept().setText(formatException(exception)));
        return operationOutcome;
    }

    public static OperationOutcome createFromMediaTypeNotSupportedException
        (HttpMediaTypeNotSupportedException exception) {
        var operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setCode(NOTSUPPORTED)
            .setSeverity(ERROR)
            .setDetails(new CodeableConcept().setText(formatException(exception)));
        return operationOutcome;
    }

    private static String formatException(Throwable exception) {
        var errors = new StringWriter();
        exception.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}
