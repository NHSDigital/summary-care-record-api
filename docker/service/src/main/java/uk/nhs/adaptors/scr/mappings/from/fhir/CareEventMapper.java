package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.models.xml.CareEvent;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CareEventMapper {
    public static CareEvent mapCareEvent(Encounter encounter) {
        var careEvent = new CareEvent();

        careEvent.setIdRoot("0F582D91-8F89-11EA-8B2D-B741F13EFC47");

        return careEvent;
    }
}
