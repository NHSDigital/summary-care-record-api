package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.xml.PatientCarerCorrespondence;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientAndCarersCorrespondenceMapper {

    private final UuidWrapper uuid;

    public PatientCarerCorrespondence mapPatientCarerCorrespondence(Communication communication) {
        var patientCarerCorr = new PatientCarerCorrespondence();

        patientCarerCorr.setIdRoot(uuid.randomUuid());

        patientCarerCorr.setStatusCodeCode("normal");

        var codingFirstRep = communication.getTopic().getCodingFirstRep();
        patientCarerCorr.setCodeCode(codingFirstRep.getCode());
        patientCarerCorr.setCodeDisplayName(codingFirstRep.getDisplay());

        patientCarerCorr.setEffectiveTimeLow(formatDateToHl7(communication.getSentElement()));

        return patientCarerCorr;
    }

}
