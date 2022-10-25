package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.models.xml.Finding;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindingMapper {

    public Finding mapFinding(Observation observation) {
        var finding = new Finding();

        finding.setIdRoot(observation.getIdentifierFirstRep().getValue());

        var codingFirstRep = observation.getCode().getCodingFirstRep();

        finding.setCodeCode(codingFirstRep.getCode());
        finding.setCodeDisplayName(codingFirstRep.getDisplay());
        finding.setStatusCodeCode("completed");

        var period = observation.getEffectivePeriod();
        if (period.hasStart()) {
            finding.setEffectiveTimeLow(formatDateToHl7(period.getStartElement()));
        }
        if (period.hasEnd()) {
            finding.setEffectiveTimeHigh(formatDateToHl7(period.getEndElement()));
        }

        return finding;
    }
}
