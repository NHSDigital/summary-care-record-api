package uk.nhs.adaptors.scr.mappings.from.hl7.common;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.COMPLETED;
import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.INPROGRESS;
import static org.hl7.fhir.r4.model.Procedure.ProcedureStatus.NULL;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.SNOMED_SYSTEM;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getNodesByXPath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalValueByXPath;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getValueByXPath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProcedureHL7Mapper {

    private static final String UK_CORE_PROCEDURE_PROFILE = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Procedure";
    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String EFFECTIVE_TIME_CENTRE_XPATH = "./effectiveTime/centre/@value";
    private static final String PERTINENT_CRET_BASE_PATH = GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType[.//%s]";
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String ENTRY_BASE_PATH = "./component/%s";

    private final CodedEntryMapper codedEntryMapper;

    public List<Resource> map(Node document, String xmlNodeName) {
        List<Resource> resources = new ArrayList<>();
        for (var pertinentCREType : getNodesByXPath(document, String.format(PERTINENT_CRET_BASE_PATH, xmlNodeName))) {
            var pertinentCRETypeCode = getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            for (Node node : getNodesByXPath(pertinentCREType, String.format(ENTRY_BASE_PATH, xmlNodeName))) {
                Procedure procedure = mapProcedure(node);
                procedure.setCategory(new CodeableConcept(new Coding()
                    .setSystem(SNOMED_SYSTEM)
                    .setCode(pertinentCRETypeCode)
                    .setDisplay(pertinentCRETypeDisplay)));
                resources.add(procedure);

            }
        }
        return resources;
    }

    public Procedure mapProcedure(Node node) {
        var effectiveTimeCentre =
            getOptionalValueByXPath(node, EFFECTIVE_TIME_CENTRE_XPATH).map(XmlToFhirMapper::parseDate);

        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

        Procedure procedure = new Procedure();
        procedure.setId(entry.getId());
        procedure.addIdentifier(new Identifier().setValue(entry.getId()));
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
