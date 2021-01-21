package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PatientId;

import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.FhirHelper.NHS_NUMBER_IDENTIFIER_SYSTEM;

@Component
public class PatientMapper {

    public static void mapPatient(GpSummary gpSummary, Patient patient) throws FhirMappingException {
        setPatientIds(gpSummary, patient);
    }

    private static void setPatientIds(GpSummary gpSummary, Patient patient) throws FhirMappingException {
        var patientIds = patient.getIdentifier().stream()
            .filter(identifier -> NHS_NUMBER_IDENTIFIER_SYSTEM.equals(identifier.getSystem()))
            .peek(identifier -> {
                if (StringUtils.isBlank(identifier.getValue())) {
                    throw new FhirMappingException(String.format(
                        "patient.identifier[].value for system %s must not be empty", NHS_NUMBER_IDENTIFIER_SYSTEM));
                }
            })
            .map(Identifier::getValue)
            .map(PatientId::new)
            .collect(Collectors.toList());

        if (patientIds.isEmpty()) {
            throw new FhirMappingException(String.format(
                "patient.identifier[] for system %s must not be empty", NHS_NUMBER_IDENTIFIER_SYSTEM));
        }

        gpSummary.setPatientIds(patientIds);
    }
}
