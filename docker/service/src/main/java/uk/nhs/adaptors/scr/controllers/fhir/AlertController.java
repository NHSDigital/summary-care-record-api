package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.scr.controllers.validation.alert.AlertRequest;
import uk.nhs.adaptors.scr.services.AlertService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.HttpStatus.CREATED;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_IDENTITY;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_SESSION_URID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Validated
public class AlertController {

    private final AlertService alertService;

    @PostMapping(path = "/AuditEvent",
        produces = APPLICATION_FHIR_JSON_VALUE,
        consumes = APPLICATION_FHIR_JSON_VALUE)
    @ResponseStatus(CREATED)
    public void sendAlert(@RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
                          @RequestHeader(NHSD_IDENTITY) @NotNull String nhsdIdentity,
                          @RequestHeader(NHSD_SESSION_URID) @NotNull String nhsdSessionUrid,
                          @RequestBody @AlertRequest String body) {

        alertService.sendAlert(body, nhsdAsid, nhsdIdentity, nhsdSessionUrid);
    }
}
