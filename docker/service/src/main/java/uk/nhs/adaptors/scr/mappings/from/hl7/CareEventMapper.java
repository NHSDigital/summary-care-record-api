package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterStatus;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;
import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.INPROGRESS;
import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.NULL;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CareEventMapper implements XmlToFhirMapper {

    private static final String UK_CORE_ENCOUNTER_PROFILE = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Encounter";
    private static final String ACT_CODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ActCode";
    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String CARE_EVENT_BASE_PATH =
        GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType/component/UKCT_MT144037UK01.CareEvent";

    private final CodedEntryMapper codedEntryMapper;

    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();
        for (Node node : XmlUtils.getNodesByXPath(document, CARE_EVENT_BASE_PATH)) {
            CommonCodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

            Encounter encounter = new Encounter();
            encounter.setId(entry.getId());
            encounter.setMeta(new Meta().addProfile(UK_CORE_ENCOUNTER_PROFILE));

            encounter.setClass_(new Coding()
                .setSystem(ACT_CODE_SYSTEM)
                .setCode("GENRL")
                .setDisplay("General"));

            encounter.addType(new CodeableConcept(new Coding()
                .setSystem(SNOMED_SYSTEM)
                .setCode(entry.getCodeValue())
                .setDisplay(entry.getCodeDisplay())));

            encounter.setStatus(mapStatus(entry.getStatus()));

            if (entry.getEffectiveTimeLow().isPresent() || entry.getEffectiveTimeHigh().isPresent()) {
                var period = new Period();
                entry.getEffectiveTimeLow().ifPresent(period::setStart);
                entry.getEffectiveTimeHigh().ifPresent(period::setEnd);
                encounter.setPeriod(period);
            }

            resources.add(encounter);

        }
        return resources;
    }

    private static EncounterStatus mapStatus(String statusCode) {
        switch (statusCode) {
            case "active":
                return INPROGRESS;
            case "normal":
            case "completed":
                return FINISHED;
            case "nullified":
                return NULL;
            default:
                throw new IllegalArgumentException(statusCode);
        }
    }
}
