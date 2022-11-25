package uk.nhs.adaptors.scr.mappings.from.fhir;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.xml.CareProfessionalDocumentation;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CareProfessionalDocumentationMapper {

    public CareProfessionalDocumentation map(Communication communication) {
        var obj = new CareProfessionalDocumentation();
        var codingFirstRep = communication.getTopic().getCodingFirstRep();

        obj.setIdRoot(communication.getIdentifierFirstRep().getValue());
        obj.setStatusCodeCode("completed");
        obj.setCodeCode(codingFirstRep.getCode());
        obj.setCodeDisplayName(codingFirstRep.getDisplay());

        var sentDate = communication.getSentElement();
        sentDate.setPrecision(TemporalPrecisionEnum.SECOND);
        obj.setEffectiveTime(formatDateToHl7(sentDate));

        return obj;
    }
}

