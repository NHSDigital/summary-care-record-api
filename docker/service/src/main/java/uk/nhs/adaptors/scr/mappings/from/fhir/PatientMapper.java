package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.GpSummary;

import static uk.nhs.adaptors.scr.utils.FhirHelper.NHS_NUMBER_IDENTIFIER_SYSTEM;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResource;

/**
 * Map the patient section from FHIR's "entry.patient" resource type, into HL7.
 * Called by BundleMapper's call to GpSummary.fromBundle.
 */
@Component
public class PatientMapper {

    public static void mapPatient(GpSummary gpSummary, Bundle bundle) throws FhirMappingException {
        Patient patient = getDomainResource(bundle, Patient.class);
        setPatientIds(gpSummary, patient);
        var composition = getDomainResource(bundle, Composition.class);
        if (!composition.hasSubject() || !patient.getId().contains(composition.getSubject().getReference())) {
            throw new FhirValidationException("Composition.subject reference is missing or invalid");
        }
    }

    private static void setPatientIds(GpSummary gpSummary, Patient patient) throws FhirMappingException {
        patient.getIdentifier().stream()
            .filter(identifier -> NHS_NUMBER_IDENTIFIER_SYSTEM.equals(identifier.getSystem()))
            .map(Identifier::getValue)
            .filter(StringUtils::isNotBlank)
            .reduce((a, b) -> {
                throw new FhirMappingException(String.format("Multiple Patient resource '%s' identifiers", NHS_NUMBER_IDENTIFIER_SYSTEM));
            })
            .ifPresentOrElse(
                gpSummary::setPatientId,
                () -> {
                    throw new FhirMappingException(String.format(
                        "patient.identifier[] for system %s must not be empty", NHS_NUMBER_IDENTIFIER_SYSTEM));
                });
    }
}
