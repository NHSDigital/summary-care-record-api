package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.xml.Investigation;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InvestigationMapper {

    private final UuidWrapper uuid;

    public Investigation mapInvestigation(Procedure procedure) {
        var investigation = new Investigation();

        investigation.setIdRoot(uuid.randomUuid());

        var codingFirstRep = procedure.getCode().getCodingFirstRep();
        investigation.setCodeCode(codingFirstRep.getCode());
        investigation.setCodeDisplayName(codingFirstRep.getDisplay());
        investigation.setStatusCodeCode("normal");

        investigation.setEffectiveTimeLow(formatDateToHl7(procedure.getPerformedDateTimeType()));

        return investigation;
    }
}
