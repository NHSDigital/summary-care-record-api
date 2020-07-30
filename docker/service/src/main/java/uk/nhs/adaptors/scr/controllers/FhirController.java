package uk.nhs.adaptors.scr.controllers;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.services.ScrService;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FhirController {
    private final FhirParser fhirParser;
    private final ScrService scrService;

    @PostMapping(
        path = "/fhir",
        consumes = {FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE, FhirMediaTypes.APPLICATION_FHIR_XML_VALUE},
        produces = {FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE, FhirMediaTypes.APPLICATION_FHIR_XML_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> acceptFhir(
        @RequestHeader("Content-Type") MediaType contentType, @RequestBody String body)
        throws FhirValidationException, HttpMediaTypeNotAcceptableException {

        IBaseResource resource = fhirParser.parseResource(contentType, body);
        scrService.handleFhir(resource);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
