package uk.nhs.adaptors.scr.fhirmappings;

import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;

import java.util.List;

import static uk.nhs.adaptors.scr.utils.FhirHelper.getNhsNumber;

@Component
public class PatientMapper {

    public static void mapPatient(GpSummary gpSummary, Patient patient) throws FhirMappingException {
        gpSummary.setPatientIds(List.of(getNhsNumber(patient)));
    }
}
