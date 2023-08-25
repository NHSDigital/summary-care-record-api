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
import uk.nhs.adaptors.scr.controllers.validation.acs.AcsRequest;
import uk.nhs.adaptors.scr.models.RequestData;
import uk.nhs.adaptors.scr.services.AcsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletResponse;

import javax.validation.constraints.NotNull;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_IP;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_SESSION_URID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Validated
public class AcsController {

    private final AcsService acsService;

    @PostMapping(
        path = "/$setPermission",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE})
    public ResponseEntity<String> setPermission(
        @RequestBody @AcsRequest String parameters,
        @RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
        @RequestHeader(CLIENT_IP) @NotNull String clientIp,
        @RequestHeader(NHSD_SESSION_URID) @NotNull String nhsdSessionUrid,
        @RequestHeader(AUTHORIZATION) @NotNull String authorization,
        HttpServletResponse response) {
        LOGGER.info("Received ACS Set Permission request");
        RequestData requestData = new RequestData().setBody(parameters)
            .setClientIp(clientIp)
            .setNhsdAsid(nhsdAsid)
            .setNhsdSessionUrid(nhsdSessionUrid)
            .setAuthorization(authorization);
        String responseBody = acsService.setPermission(requestData);

        // Set the response body and return appropriate status code
        response.setStatus(HttpStatus.CREATED.value()); // You can set the desired status code here
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }
}
