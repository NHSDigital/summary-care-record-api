package uk.nhs.adaptors.scr.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Resource;

import uk.nhs.adaptors.scr.exceptions.FhirMappingException;

public class FhirHelper {

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

    public static List<Resource> getDomainResourceList(Bundle bundle, Enumerations.ResourceType resourceType) {
        return bundle.getEntry().stream()
            .map(BundleEntryComponent::getResource)
            .filter(resType -> resType.getResourceType().toString().toLowerCase().equals(resourceType.toString().toLowerCase()))
            .collect(Collectors.toList());
    }
}
