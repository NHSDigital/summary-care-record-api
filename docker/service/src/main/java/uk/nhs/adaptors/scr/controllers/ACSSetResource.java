package uk.nhs.adaptors.scr.controllers;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.scr.models.ACSPayload;
import uk.nhs.adaptors.scr.services.ACSService;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ACSSetResource {
    private final ACSService acsService;

    @PostMapping(
        path = "/summary-care-record/consent",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> acceptACSSetResource(
        @RequestBody ACSPayload acsSetResourceObject) {

        try {
            String setResourcePermissionsINUK01 = acsService.handleMessage(acsSetResourceObject);
        } catch (IOException e) {
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(OK);
    }
}
