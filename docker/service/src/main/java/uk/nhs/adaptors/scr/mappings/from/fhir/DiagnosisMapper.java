package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.xml.Diagnosis;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from FHIR to HL7 for a diagnosis
 *
 * CMET: UKCT_MT144042UK01
 */
public class DiagnosisMapper {

    private final UuidWrapper uuid;

    public Diagnosis mapDiagnosis(Condition condition) {
        var diagnosis = new Diagnosis();

        diagnosis.setIdRoot(uuid.randomUuid());

        var codingFirstRep = condition.getCode().getCodingFirstRep();

        diagnosis.setCodeCode(codingFirstRep.getCode());
        diagnosis.setCodeDisplayName(codingFirstRep.getDisplay());

        if (condition.hasOnsetDateTimeType()) {
            diagnosis.setEffectiveTimeLow(formatDateToHl7(condition.getOnsetDateTimeType()));
        }

        return diagnosis;
    }

}
