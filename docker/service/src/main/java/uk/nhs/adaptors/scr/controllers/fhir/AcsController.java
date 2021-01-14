package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.models.RequestData;
import uk.nhs.adaptors.scr.services.AcsService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_IP;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_REQUEST_URL;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_SESSION_URID;
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
                              @RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
                              @RequestHeader(CLIENT_IP) @NotNull String clientIp,
                              @RequestHeader(CLIENT_REQUEST_URL) @NotNull String clientRequestUrl,
                              @RequestHeader(NHSD_SESSION_URID) @NotNull String nhsdSessionUrid,
                              @RequestHeader(AUTHORIZATION) @NotNull String authorization) {
        RequestData requestData = new RequestData().setBody(parameters)
            .setClientRequestUrl(clientRequestUrl)
            .setClientIp(clientIp)
            .setNhsdAsid(nhsdAsid)
            .setNhsdSessionUrid(nhsdSessionUrid)
            .setAuthorization(authorization);
        acsService.setPermission(requestData);
    }
}
