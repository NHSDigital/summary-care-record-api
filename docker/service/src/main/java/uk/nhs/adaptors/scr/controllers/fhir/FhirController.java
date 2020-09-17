package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.services.ScrService;

import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_XML_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FhirController {
    private final FhirParser fhirParser;
    private final ScrService scrService;
    private final SpineConfiguration spineConfiguration;

    @PostMapping(
        path = "/fhir",
        consumes = {APPLICATION_FHIR_JSON_VALUE, APPLICATION_FHIR_XML_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE, APPLICATION_FHIR_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public OperationOutcome acceptFhir(
        @RequestHeader("Content-Type") MediaType contentType, @RequestBody String body)
        throws FhirValidationException, HttpMediaTypeNotAcceptableException, FhirMappingException {

        try {
            Bundle bundle = fhirParser.parseResource(contentType, body);
            scrService.handleFhir(bundle);

            return new OperationOutcome();
        } catch (Exception e) {
            throw e;
        }
    }
}
