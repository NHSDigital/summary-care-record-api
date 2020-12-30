package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.scr.clients.SpineClientContract;

import javax.validation.constraints.NotNull;

import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_IDENTITY;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_SESSION_URID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AlertController {

    @Autowired
    private final SpineClientContract spineClient;

    @PostMapping(path = "/AuditEvent",
        produces = APPLICATION_FHIR_JSON_VALUE,
        consumes = APPLICATION_FHIR_JSON_VALUE)
    public void sendAlert(@RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
                          @RequestHeader(NHSD_IDENTITY) @NotNull String nhsdIdentity,
                          @RequestHeader(NHSD_SESSION_URID) @NotNull String nhsdSessionUrid,
                          @RequestBody String body) {

        spineClient.sendAlert(body, nhsdAsid, nhsdIdentity, nhsdSessionUrid);
    }
}
