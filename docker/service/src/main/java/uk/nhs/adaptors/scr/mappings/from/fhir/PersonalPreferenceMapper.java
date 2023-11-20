package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.xml.PersonalPreference;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from FHIR to HL7 to represent personal preferences of the patient
 *
 * CMET: UKCT_MT144046UK01
 */
public class PersonalPreferenceMapper {

    private final UuidWrapper uuid;

    public PersonalPreference mapPersonalPreference(Observation observation) {
        var personalPreference = new PersonalPreference();

        personalPreference.setIdRoot(uuid.randomUuid());

        var codingFirstRep = observation.getCode().getCodingFirstRep();
        personalPreference.setCodeCode(codingFirstRep.getCode());
        personalPreference.setCodeDisplayName(codingFirstRep.getDisplay());
        personalPreference.setStatusCodeCode("completed");

        personalPreference.setEffectiveTimeLow(formatDateToHl7(observation.getEffectiveDateTimeType()));

        return personalPreference;
    }
}
