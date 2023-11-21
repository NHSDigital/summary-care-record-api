package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from HL7 to FHIR to represent personal preferences of the patient.
 *
 * CMET: UKCT_MT144046UK01
 */
public class PersonalPreferencesMapper implements XmlToFhirMapper {

    private final UuidWrapper uuid;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[."
        + "//UKCT_MT144046UK01.PersonalPreference]";
    private static final String TREATMENTS_BASE_PATH = "./component/UKCT_MT144046UK01.PersonalPreference";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";

    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList treatmentNodes = xmlUtils.getNodeListByXPath(pertinentCREType, TREATMENTS_BASE_PATH);
            for (int j = 0; j < treatmentNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(treatmentNodes, j);
                mapObservation(resources, node);
            }
        }
        return resources;
    }

    private void mapObservation(List<Resource> resources, Node node) {
        var observation = new Observation();

        observation.setId(uuid.randomUuid());
        observation.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_META));
        observation.setStatus(Observation.ObservationStatus.FINAL);

        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

        var coding = new CodeableConcept().addCoding(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(entry.getCodeValue())
            .setDisplay(entry.getCodeDisplay()));
        observation.setCode(coding);

        entry.getEffectiveTimeLow().ifPresent(observation::setEffective);

        resources.add(observation);
    }
}
