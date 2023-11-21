package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.models.xml.ProvisionOfAdviceAndInformation;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Maps the Provision Of Advice And Information from FHIR JSON to HL7 XML.
 *
 * CMET: UKCT_MT144049UK01
 */
public class ProvisionOfAdviceAndInfoMapper {

    public ProvisionOfAdviceAndInformation mapProvisionOfAdviceInfo(Communication communication) {
        var obj = new ProvisionOfAdviceAndInformation();
        obj.setIdRoot(communication.getIdentifierFirstRep().getValue());
        var codingFirstRep = communication.getTopic().getCodingFirstRep();

        obj.setCodeCode(codingFirstRep.getCode());
        obj.setCodeDisplayName(codingFirstRep.getDisplay());
        obj.setStatusCodeCode("normal");

        obj.setEffectiveTimeLow(formatDateToHl7(communication.getSentElement()));

        return obj;
    }
}
