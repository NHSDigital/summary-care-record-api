package uk.nhs.adaptors.sandbox.scr.controllers;

import org.springframework.http.MediaType;

public class FhirMediaTypes {
    public static final String APPLICATION_FHIR_JSON_VALUE = "application/fhir+json";
    public static final MediaType APPLICATION_FHIR_JSON = MediaType.parseMediaType(APPLICATION_FHIR_JSON_VALUE);
}
