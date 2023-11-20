package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.xml.Treatment;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from FHIR to HL7 to represent treatment given to the patient.
 *
 * CMET: UKCT_MT144055UK01
 */
public class TreatmentMapper {

    public Treatment mapTreatment(Procedure procedure) {
        var treatment = new Treatment();

        treatment.setIdRoot(procedure.getIdentifierFirstRep().getValue());

        var codingFirstRep = procedure.getCode().getCodingFirstRep();
        treatment.setCodeCode(codingFirstRep.getCode());
        treatment.setCodeDisplayName(codingFirstRep.getDisplay());
        treatment.setStatusCodeCode("normal");


        if (procedure.getPerformed() instanceof DateTimeType) {
            treatment.setEffectiveTimeLow(formatDateToHl7(procedure.getPerformedDateTimeType()));
        } else if (procedure.getPerformed() instanceof Period) {
            var period = procedure.getPerformedPeriod();
            if (period.hasStart()) {
                treatment.setEffectiveTimeLow(formatDateToHl7(period.getStartElement()));
            }
            if (period.hasEnd()) {
                treatment.setEffectiveTimeHigh(formatDateToHl7(period.getEndElement()));
            }
        } else {
            throw new FhirValidationException("procedure.performed must be of type DateTimeType or Period");
        }

        return treatment;
    }
}
