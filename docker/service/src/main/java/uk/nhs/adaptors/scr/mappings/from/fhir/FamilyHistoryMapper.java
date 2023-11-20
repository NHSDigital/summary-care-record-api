package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.xml.FamilyHistory;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from FHIR to HL7 to represent observations related to family history.
 *
 * CMET: UKCT_MT144044UK01
 */
public class FamilyHistoryMapper {

    public FamilyHistory map(Observation observation) {
        var familyHistory = new FamilyHistory();

        familyHistory.setIdRoot(observation.getIdentifierFirstRep().getValue());

        var codingFirstRep = observation.getCode().getCodingFirstRep();
        familyHistory.setCodeCode(codingFirstRep.getCode());
        familyHistory.setCodeDisplayName(codingFirstRep.getDisplay());
        familyHistory.setStatusCodeCode(observation.getStatusElement().getCode());

        if (observation.getEffective() instanceof DateTimeType) {
            familyHistory.setEffectiveTimeLow(formatDateToHl7(observation.getEffectiveDateTimeType()));
        } else if (observation.getEffective() instanceof Period) {
            var period = observation.getEffectivePeriod();
            if (period.hasStart()) {
                familyHistory.setEffectiveTimeLow(formatDateToHl7(period.getStartElement()));
            }
            if (period.hasEnd()) {
                familyHistory.setEffectiveTimeHigh(formatDateToHl7(period.getEndElement()));
            }
        } else {
            throw new FhirValidationException("Observation.effective must be of type DateTimeType or Period");
        }

        return familyHistory;
    }
}

