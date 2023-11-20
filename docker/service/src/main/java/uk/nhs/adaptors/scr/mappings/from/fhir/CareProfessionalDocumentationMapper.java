package uk.nhs.adaptors.scr.mappings.from.fhir;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import uk.nhs.adaptors.scr.models.xml.CareProfessionalDocumentation;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from FHIR to HL7 for Care professional documentation.
 * Despite having the same CMET number, Third party correspondence, and Patient or carer correspondence are
 * mapped separately.
 *
 * CMET: UKCT_MT144035UK01
 * @see: NIAD-2321
 */
public class CareProfessionalDocumentationMapper {

    public CareProfessionalDocumentation map(Communication communication) {
        var obj = new CareProfessionalDocumentation();
        var codingFirstRep = communication.getTopic().getCodingFirstRep();

        obj.setIdRoot(communication.getIdentifierFirstRep().getValue());
        obj.setCodeCode(codingFirstRep.getCode());
        obj.setCodeDisplayName(codingFirstRep.getDisplay());
        obj.setStatusCodeCode("completed");

        var sentDate = communication.getSentElement();
        sentDate.setPrecision(TemporalPrecisionEnum.SECOND);
        obj.setEffectiveTime(formatDateToHl7(sentDate));

        return obj;
    }
}

