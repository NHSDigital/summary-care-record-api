package uk.nhs.adaptors.scr;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;

@Configuration
public class ScrSpringConfiguration {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forDstu3();
    }
}
