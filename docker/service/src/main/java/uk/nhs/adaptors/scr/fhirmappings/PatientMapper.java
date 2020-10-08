package uk.nhs.adaptors.scr.fhirmappings;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PatientId;

@Component
public class PatientMapper {

    public static void mapPatient(GpSummary gpSummary, Patient patient) throws FhirMappingException {
        setPatientIds(gpSummary, patient);
    }

    private static void setPatientIds(GpSummary gpSummary, Patient patient) throws FhirMappingException {
        List<PatientId> patientIds = new ArrayList<>();

        if (patient.hasIdentifier()) {
            for (Identifier identifier : patient.getIdentifier()) {
                if (identifier.hasValue()) {
                    PatientId patientId = new PatientId();
                    patientId.setPatientId(identifier.getValue());
                    patientIds.add(patientId);
                }
            }
        }

        gpSummary.setPatientIds(patientIds);
    }
}
