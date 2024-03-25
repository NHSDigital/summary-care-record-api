package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.xml.CareEvent;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from FHIR to HL7 for Care Events.
 * Recording of care events including but not limited to those which are:
 * Intended, Requested, Promised, Proposed, Booked.
 *
 * CMET: UKCT_MT144037UK01
 * @see: NIAD-2316
 */
public class CareEventMapper {

    private final UuidWrapper uuid;

    public CareEvent mapCareEvent(Encounter encounter) {
        var careEvent = new CareEvent();

        careEvent.setIdRoot(uuid.randomUuid());

        var codingFirstRep = encounter.getTypeFirstRep().getCodingFirstRep();

        careEvent.setCodeCode(codingFirstRep.getCode());
        careEvent.setCodeDisplayName(codingFirstRep.getDisplay());
        careEvent.setStatusCodeCode("normal");

        if (encounter.getPeriod().hasStart()) {
            careEvent.setEffectiveTimeHigh(formatDateToHl7(encounter.getPeriod().getStartElement()));
        }
        if (encounter.getPeriod().hasEnd()) {
            careEvent.setEffectiveTimeLow(formatDateToHl7(encounter.getPeriod().getEndElement()));
        }

        return careEvent;
    }
}
