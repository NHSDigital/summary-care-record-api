package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Communication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.xml.ProvisionOfAdviceAndInformation;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProvisionOfAdviceAndInfoMapper {

    private final UuidWrapper uuid;

    public ProvisionOfAdviceAndInformation mapProvisionOfAdviceInfo(Communication communication) {
        var provisionOfAdviceInfo = new ProvisionOfAdviceAndInformation();

        provisionOfAdviceInfo.setIdRoot(uuid.randomUuid());

        var codingFirstRep = communication.getTopic().getCodingFirstRep();
        provisionOfAdviceInfo.setCodeCode(codingFirstRep.getCode());
        provisionOfAdviceInfo.setCodeDisplayName(codingFirstRep.getDisplay());
        provisionOfAdviceInfo.setStatusCodeCode("normal");

        provisionOfAdviceInfo.setEffectiveTimeLow(formatDateToHl7(communication.getSentElement()));

        return provisionOfAdviceInfo;
    }
}
