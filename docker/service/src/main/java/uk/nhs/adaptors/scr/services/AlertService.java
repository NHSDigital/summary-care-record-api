package uk.nhs.adaptors.scr.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.SpineClientContract;
import uk.nhs.adaptors.scr.clients.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlertService {
    private final SpineClientContract spineClient;
    private final FhirParser fhirParser;

    public void sendAlert(String body, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        Response response = spineClient.sendAlert(body, nhsdAsid, nhsdIdentity, nhsdSessionUrid);

        if (response.getStatusCode() != OK.value()) {
            OperationOutcome error = fhirParser.parseResource(response.getBody(), OperationOutcome.class);
            if (response.getStatusCode() == BAD_REQUEST.value()) {
                throw new BadRequestException(getErrorReason(error));
            } else {
                throw new UnexpectedSpineResponseException(getErrorReason(error));
            }
        }
    }

    private String getErrorReason(OperationOutcome error) {
        Coding coding = error.getIssueFirstRep().getDetails().getCodingFirstRep();
        return coding.getDisplay();
    }
}
