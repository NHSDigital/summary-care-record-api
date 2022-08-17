package uk.nhs.adaptors.scr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.spine.SpineClientContract;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlertService {
    private static final String NHS_ASID_SYSTEM = "https://fhir.nhs.uk/Id/nhsSpineASID";
    private final SpineClientContract spineClient;
    private final FhirParser fhirParser;

    public void sendAlert(String body, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        AuditEvent auditEvent = fhirParser.parseResource(body, AuditEvent.class);

        if (!auditEvent.getSource().getObserver().getIdentifier().hasValue()) {
            var identifier = new Identifier();
            identifier.setSystem(NHS_ASID_SYSTEM);
            identifier.setValue(nhsdAsid);
            auditEvent.getSource().getObserver().setIdentifier(identifier);
        }

        Response<String> response = spineClient.sendAlert(fhirParser.encodeToJson(auditEvent), nhsdAsid, nhsdIdentity, nhsdSessionUrid);

        HttpStatus status = HttpStatus.resolve(response.getStatusCode());
        if (status == null || !status.is2xxSuccessful()) {
            OperationOutcome error = fhirParser.parseResource(response.getBody(), OperationOutcome.class);
            if (status != null && status.is4xxClientError()) {
                LOGGER.error("Spine processing error: {}", status);
                throw new BadRequestException(getErrorReason(error));
            } else {
                LOGGER.error("Spine processing error: {}", getErrorReason(error));
                throw new UnexpectedSpineResponseException(getErrorReason(error));
            }
        }
    }

    private String getErrorReason(OperationOutcome error) {
        Coding coding = error.getIssueFirstRep().getDetails().getCodingFirstRep();
        return coding.getDisplay();
    }
}
