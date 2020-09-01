package uk.nhs.adaptors.scr.fhirmappings;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;

import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerId;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerName;

public class PractitionerMapper {
    public static void mapPractitioner(GpSummary gpSummary, Practitioner practitioner) throws FhirMappingException {
        setPractitionerIds(gpSummary, practitioner);
        setPractitionerNames(gpSummary, practitioner);
    }

    private static void setPractitionerIds(GpSummary gpSummary, Practitioner practitioner) throws FhirMappingException {
        List<PractitionerId> practitionerIds = new ArrayList<>();

        if (practitioner.hasIdentifier()) {
            for (Identifier identifier : practitioner.getIdentifier()) {
                if (identifier.hasValue()) {
                    PractitionerId practitionerId = new PractitionerId();
                    practitionerId.setPractitionerId(identifier.getValue());
                    practitionerIds.add(practitionerId);
                }
            }
        }

        gpSummary.setPractitionerIds(practitionerIds);
    }

    private static void setPractitionerNames(GpSummary gpSummary, Practitioner practitioner) throws FhirMappingException {
        List<PractitionerName> practitionerNames = new ArrayList<>();

        if (practitioner.hasName()) {
            for (HumanName humanName : practitioner.getName()) {
                if (humanName.getNameAsSingleString() != null) {
                    PractitionerName practitionerName = new PractitionerName();
                    practitionerName.setPractitionerName(humanName.getNameAsSingleString());
                    practitionerNames.add(practitionerName);
                }
            }
        }

        gpSummary.setPractitionerNames(practitionerNames);
    }
}
