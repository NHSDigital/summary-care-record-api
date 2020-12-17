package uk.nhs.adaptors.scr.controllers.acs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.services.AcsService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.HttpStatus.CREATED;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AcsController {

    private final FhirParser fhirParser;
    private final AcsService acsService;

    @PostMapping(
        path = "/$setPermission",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE})
    @ResponseStatus(CREATED)
    public void setPermission(@RequestBody String parameters,
                              @RequestHeader("Nhsd-Asid") @NotNull String nhsdAsid,
                              @RequestHeader("client-ip") @NotNull String clientIp) {
        acsService.setPermission(fhirParser.parseResource(parameters, Parameters.class), nhsdAsid, clientIp);
    }
}
