package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.COMPLETED;
import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.INPROGRESS;
import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.NULL;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.SNOMED_SYSTEM;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalValueByXPath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProcedureMapper {

    private static final String UK_CORE_PROCEDURE_PROFILE = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Procedure";
    private static final String EFFECTIVE_TIME_CENTRE_XPATH = "./effectiveTime/centre/@value";

    private final CodedEntryMapper codedEntryMapper;

    public Procedure mapProcedure(Node node) {
        var effectiveTimeCentre =
            getOptionalValueByXPath(node, EFFECTIVE_TIME_CENTRE_XPATH).map(XmlToFhirMapper::parseDate);

        CommonCodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

        Procedure procedure = new Procedure();
        procedure.setId(entry.getId());
        procedure.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_PROFILE));

        procedure.setCode(new CodeableConcept(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(entry.getCodeValue())
            .setDisplay(entry.getCodeDisplay())));
        procedure.setStatus(mapStatus(entry.getStatus()));

        if (entry.getEffectiveTimeLow().isPresent() || entry.getEffectiveTimeHigh().isPresent()) {
            var period = new Period();
            entry.getEffectiveTimeLow().ifPresent(period::setStart);
            entry.getEffectiveTimeHigh().ifPresent(period::setEnd);
            procedure.setPerformed(period);
        } else {
            effectiveTimeCentre
                .map(DateTimeType::new)
                .ifPresent(procedure::setPerformed);
        }
        return procedure;
    }

    private static Procedure.ProcedureStatus mapStatus(String statusCode) {
        switch (statusCode) {
            case "active":
                return INPROGRESS;
            case "completed":
            case "normal":
                return COMPLETED;
            case "nullified":
                return NULL;
            default:
                throw new IllegalArgumentException(statusCode);
        }
    }
}
