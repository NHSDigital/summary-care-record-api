package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.models.xml.CareEvent;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CareEventMapper {
    public static CareEvent mapCareEvent(Encounter encounter) {
        var careEvent = new CareEvent();

        careEvent.setIdRoot("0F582D91-8F89-11EA-8B2D-B741F13EFC47");
        careEvent.setCodeCode(encounter.getType().get(0).getCoding().get(0).getCode());
        careEvent.setCodeDisplayName(encounter.getType().get(0).getCoding().get(0).getDisplay());
        careEvent.setStatusCodeCode("normal");

        if(encounter.getPeriod().hasStart()) {
            careEvent.setEffectiveTimeHigh(formatDateToHl7(encounter.getPeriod().getStartElement()));
        }
        if(encounter.getPeriod().hasEnd()) {
            careEvent.setEffectiveTimeLow(formatDateToHl7(encounter.getPeriod().getEndElement()));
        }

        return careEvent;
    }
}
