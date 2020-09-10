package uk.nhs.adaptors.scr.hl7tofhirmappers;

import static org.hl7.fhir.r4.model.IdType.newRandomUuid;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

import uk.nhs.adaptors.scr.models.hl7models.PatientObject;

public class PatientMapper {
    public Patient mapPatient(PatientObject patientObject) {
        Patient patient = new Patient();

        patient.setId(newRandomUuid());

        patient.addIdentifier(new Identifier()
            .setValue(patientObject.getNhsNumber()) //done
            .setSystem("https://fhir.nhs.uk/Id/nhs-number")); //done

        return patient;
    }
}
