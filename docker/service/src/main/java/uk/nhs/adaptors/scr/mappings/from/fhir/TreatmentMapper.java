package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.xml.Investigation;
import uk.nhs.adaptors.scr.models.xml.Treatment;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TreatmentMapper {

    private final UuidWrapper uuid;

    public Treatment mapTreatment(Procedure procedure) {
        var treatment = new Treatment();

        treatment.setIdRoot(uuid.randomUuid());

        var codingFirstRep = procedure.getCode().getCodingFirstRep();
        treatment.setCodeCode(codingFirstRep.getCode());
        treatment.setCodeDisplayName(codingFirstRep.getDisplay());
        treatment.setStatusCodeCode("normal");

        treatment.setEffectiveTimeLow(formatDateToHl7(procedure.getPerformedDateTimeType()));

        return treatment;
    }
}
