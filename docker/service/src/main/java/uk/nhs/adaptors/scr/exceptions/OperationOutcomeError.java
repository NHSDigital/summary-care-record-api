package uk.nhs.adaptors.scr.exceptions;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.http.HttpStatus;

public interface OperationOutcomeError {
    OperationOutcome getOperationOutcome();

    HttpStatus getStatusCode();
}
