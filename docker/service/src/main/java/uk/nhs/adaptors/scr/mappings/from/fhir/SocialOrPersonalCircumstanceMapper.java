package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.xml.SocialOrPersonalCircumstance;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from FHIR to HL7 to represent Social or Personal Circumstances.
 * Closely related to lifestyle.
 * Part of the "Observation" resource type.
 *
 * CMET: UKCT_MT144036UK01
 * SNOMED: 163021000000107
 * @see: NIAD-2324
 */
public class SocialOrPersonalCircumstanceMapper {

    public SocialOrPersonalCircumstance map(Observation observation) {
        var obj = new SocialOrPersonalCircumstance();
        obj.setIdRoot(observation.getIdentifierFirstRep().getValue());
        var codingFirstRep = observation.getCode().getCodingFirstRep();

        obj.setCodeCode(codingFirstRep.getCode());
        obj.setCodeDisplayName(codingFirstRep.getDisplay());
        obj.setStatusCodeCode("normal");

        if (observation.getEffective() instanceof DateTimeType) {
            obj.setEffectiveTimeLow(formatDateToHl7(observation.getEffectiveDateTimeType()));
        } else if (observation.getEffective() instanceof Period) {
            var period = observation.getEffectivePeriod();
            if (period.hasStart()) {
                obj.setEffectiveTimeLow(formatDateToHl7(period.getStartElement()));
            }
            if (period.hasEnd()) {
                obj.setEffectiveTimeHigh(formatDateToHl7(period.getEndElement()));
            }
        } else {
            throw new FhirValidationException("Observation.effective must be of type DateTimeType or Period");
        }

        return obj;
    }
}
