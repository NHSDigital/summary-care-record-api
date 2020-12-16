package uk.nhs.adaptors.scr.utils;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PatientId;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FhirHelper {

    public static final String NHS_NUMBER_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";
    public static final String UUID_IDENTIFIER_SYSTEM = "https://tools.ietf.org/html/rfc4122";

    public static <T extends Resource> T getDomainResource(Bundle bundle, Class<T> resourceType) {
        return bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(resource -> resource.getClass() == resourceType)
            .map(resourceType::cast)
            .reduce((a, b) -> {
                throw new FhirMappingException("There is more than 1 resource of type " + resourceType.getSimpleName());
            })
            .orElseThrow(() -> new FhirMappingException(resourceType.getSimpleName() + " missing from payload"));
    }

    public static List<Resource> getDomainResourceList(Bundle bundle, ResourceType resourceType) {
        return bundle.getEntry().stream()
            .map(BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType() == resourceType)
            .collect(Collectors.toList());
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public static PatientId getNhsNumber(Patient patient) {
        return patient.getIdentifier().stream()
            .filter(identifier -> NHS_NUMBER_IDENTIFIER_SYSTEM.equals(identifier.getSystem()))
            .peek(identifier -> {
                if (StringUtils.isBlank(identifier.getValue())) {
                    throw new FhirMappingException(String.format(
                        "patient.identifier[].value for system %s must not be empty", NHS_NUMBER_IDENTIFIER_SYSTEM));
                }
            })
            .map(Identifier::getValue)
            .map(PatientId::new)
            .reduce((x, y) -> {
                throw new FhirMappingException("Petient.identifier[] must contain 1 NHS Number");
            })
            .orElseThrow(() -> new FhirMappingException(String.format(
                "patient.identifier[] for system %s must not be empty", NHS_NUMBER_IDENTIFIER_SYSTEM))
            );

    }
}
