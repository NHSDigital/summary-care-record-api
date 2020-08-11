package uk.nhs.adaptors.scr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.models.ACSPayload;
import uk.nhs.adaptors.scr.services.ACSService;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ACSSetResource {
    private final ACSService acsService;

    @Autowired
    private SpineClient spineClient;

    @PostMapping(
        path = "/summary-care-record/set-consent",
        consumes = {MediaType.APPLICATION_JSON_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> acceptACSSetResource(
        @RequestBody ACSPayload acsSetResourceObject) {

        String setResourcePermissionsINUK01 = acsService.handleMessage(acsSetResourceObject);

        return spineClient.sendToACSEndpoint(setResourcePermissionsINUK01);
    }
}
