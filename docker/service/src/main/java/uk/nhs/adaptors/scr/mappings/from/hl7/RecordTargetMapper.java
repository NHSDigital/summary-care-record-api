package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RecordTargetMapper {

    private static final String BASE_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String RECORD_TARGET_PATIENT_ID_EXTENSION_XPATH =
        BASE_XPATH + "/recordTarget/patient/id/@extension";
    private static final String NHS_NUMBER_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";

    private final XmlUtils xmlUtils;

    public Patient mapPatient(Node document) {
        var recordTargetPatientIdExtension =
            xmlUtils.getValueByXPath(document, RECORD_TARGET_PATIENT_ID_EXTENSION_XPATH);

        Patient patient = new Patient();
        patient.setId(randomUUID());
        patient.addIdentifier(new Identifier()
            .setValue(recordTargetPatientIdExtension)
            .setSystem(NHS_NUMBER_SYSTEM));
        return patient;
    }
}
