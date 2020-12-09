package uk.nhs.adaptors.sandbox.scr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static uk.nhs.adaptors.sandbox.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON;
import static uk.nhs.adaptors.sandbox.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RestController
@Slf4j
public class ScrUploadController {

    @PostMapping(
        path = "/Bundle",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE})
    @ResponseStatus(CREATED)
    public void uploadScr(@RequestHeader("Content-Type") @NotNull MediaType contentType,
                          @RequestBody String body) throws HttpMediaTypeNotAcceptableException {
        if (!contentType.equalsTypeAndSubtype(APPLICATION_FHIR_JSON)) {
            throw new HttpMediaTypeNotAcceptableException(List.of(APPLICATION_FHIR_JSON));
        }
    }
}
