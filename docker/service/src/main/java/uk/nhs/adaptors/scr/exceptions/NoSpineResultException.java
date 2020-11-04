package uk.nhs.adaptors.scr.exceptions;

import lombok.Getter;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;

@Getter
public class NoSpineResultException extends GatewayTimeoutException {

    private final long retryAfter;

    public NoSpineResultException(long retryAfter) {
        super("Spine polling yield no result");
        this.retryAfter = retryAfter;
    }

    @Override
    public IBaseOperationOutcome getOperationOutcome() {
        var operationOutcome = new OperationOutcome();

        operationOutcome.addIssue()
            .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            .setCode(OperationOutcome.IssueType.TRANSIENT)
            .setDetails(new CodeableConcept().addCoding(NHSCodings.NO_RECORD_FOUND.asCoding()))
            .setDiagnostics("Upstream server timed out. " + getMessage());

        return operationOutcome;
    }
}