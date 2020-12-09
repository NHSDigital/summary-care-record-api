package uk.nhs.adaptors.scr.exceptions;

import lombok.Getter;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.TRANSIENT;

@Getter
public class NoSpineResultException extends GatewayTimeoutException {

    private final long retryAfter;

    public NoSpineResultException(long retryAfter) {
        super("Spine polling yield no result");
        this.retryAfter = retryAfter;
    }

    @Override
    public OperationOutcome getOperationOutcome() {
        var operationOutcome = new OperationOutcome();

        operationOutcome.addIssue()
            .setSeverity(ERROR)
            .setCode(TRANSIENT)
            .setDetails(new CodeableConcept().setText("Upstream server timed out. " + getMessage()));

        return operationOutcome;
    }
}
