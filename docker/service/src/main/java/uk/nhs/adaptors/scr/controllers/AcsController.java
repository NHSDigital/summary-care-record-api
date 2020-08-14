package uk.nhs.adaptors.scr.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.exceptions.SoapClientException;
import uk.nhs.adaptors.scr.models.ACSPayload;
import uk.nhs.adaptors.scr.models.responses.ConsentsResponse;
import uk.nhs.adaptors.scr.services.AcsService;

@RestController
@RequestMapping("summary-care-record")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AcsController {
    private final AcsService acsService;

    @PostMapping(
        path = "/consent",
        consumes = {APPLICATION_JSON_VALUE},
        produces = {APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> setResourcePermissions(@RequestBody ACSPayload acsSetResourceObject) {
        acsService.setResourcePermissions(acsSetResourceObject);
        return new ResponseEntity<>(OK);
    }

    @GetMapping(
        path = "/consent/{patientId}",
        produces = {APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ConsentsResponse> getResourcePermissions(@PathVariable int patientId) {
        try {
            ConsentsResponse acsResponse = acsService.getResourcePermissions(patientId);
            return new ResponseEntity<>(acsResponse, OK);
        } catch (DocumentException e) {
            LOGGER.error(BAD_REQUEST.toString() + e.getMessage());
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Not valid XML response from ACS", e);
        } catch (SoapClientException e) {
            LOGGER.error(BAD_REQUEST.toString() + e.getMessage());
            throw new ResponseStatusException(BAD_REQUEST, e.getReason(), e);
        }
    }
}
