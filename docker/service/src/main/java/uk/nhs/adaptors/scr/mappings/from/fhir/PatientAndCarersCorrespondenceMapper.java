package uk.nhs.adaptors.scr.mappings.from.fhir;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.models.xml.PatientCarerCorrespondence;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientAndCarersCorrespondenceMapper {

    public PatientCarerCorrespondence mapPatientCarerCorrespondence(Communication communication) {
        var obj = new PatientCarerCorrespondence();
        obj.setIdRoot(communication.getIdentifierFirstRep().getValue());
        var codingFirstRep = communication.getTopic().getCodingFirstRep();

        obj.setCodeCode(codingFirstRep.getCode());
        obj.setCodeDisplayName(codingFirstRep.getDisplay());
        obj.setStatusCodeCode("normal");

        var sentDate = communication.getSentElement();
        sentDate.setPrecision(TemporalPrecisionEnum.DAY);
        obj.setEffectiveTimeLow(formatDateToHl7(sentDate));

        return obj;
    }

}

