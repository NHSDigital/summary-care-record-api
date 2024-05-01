package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from HL7 to FHIR for Care Events.
 * File & class name pluralised to avoid confusion/conflicts with mappings.from fhir.
 * Recording of care events including but not limited to those which are:
 * Intended, Requested, Promised, Proposed, Booked.
 *
 * CMET: UKCT_MT144037UK01
 * @see: NIAD-2316
 */
public class CareEventsMapper implements XmlToFhirMapper {

    private final UuidWrapper uuid;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[.//UKCT_MT144037UK01.CareEvent]";
    private static final String CARE_EVENT_BASE_PATH = "./component/UKCT_MT144037UK01.CareEvent";
    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Encounter";
    private static final String ENCOUNTER_CLASS_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ActCode";
    private List<CodeableConcept> coding = new ArrayList<>();


    public List<Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList careEventNodes = xmlUtils.getNodeListByXPath(pertinentCREType, CARE_EVENT_BASE_PATH);
            for (int j = 0; j < careEventNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(careEventNodes, j);
                mapEncounter(resources, node);
            }
        }
        return resources;
    }

    private void mapEncounter(List<Resource> resources, Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        var careEvent = new Encounter();
        careEvent.setId(uuid.randomUuid());

        if (entry.getEffectiveTimeLow().isPresent() || entry.getEffectiveTimeHigh().isPresent()) {
            var period = new Period();
            entry.getEffectiveTimeLow().ifPresent(period::setEndElement);
            entry.getEffectiveTimeHigh().ifPresent(period::setStartElement);
            careEvent.setPeriod(period);
        }

        careEvent.setMeta(new Meta().addProfile(UK_CORE_OBSERVATION_META));
        careEvent.setStatus(FINISHED);
        careEvent.setClass_(new Coding()
            .setCode("GENRL")
            .setSystem(ENCOUNTER_CLASS_SYSTEM)
            .setDisplay("General"));

        coding.add(new CodeableConcept().addCoding(new Coding()
            .setCode(entry.getCodeValue())
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(entry.getCodeDisplay())));

        careEvent.setType(coding);

        resources.add(careEvent);
    }

}
