package uk.nhs.adaptors.scr.mappings.from.hl7;

import java.util.ArrayList;
import java.util.List;
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
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

/**
 * Mapping from HL7 to FHIR for risks to patient.
 *
 * CMET: UKCT_MT144054UK01
 * @see: NIAD-2324
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RisksToPatientMapper implements XmlToFhirMapper {

    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String PERTINENT_CRET_BASE_PATH = "//pertinentInformation2/pertinentCREType[."
        + "//UKCT_MT144054UK01.RiskToPatient]";
    private static final String TREATMENTS_BASE_PATH = "./component/UKCT_MT144054UK01.RiskToPatient";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";

    /**
     * List through all XML pertinent nodes (those following CRET path <UKCR...>).
     * Detach those XML nodes and run mapObservation with them.
     *
     * @param document
     * @return
     */
    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        // Loop through <pertinentCREType> node.
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList treatmentNodes = xmlUtils.getNodeListByXPath(pertinentCREType, TREATMENTS_BASE_PATH);
            // Get "category" code/display values.
            var pertinentCRETypeCode = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            // Loop through <UKCT..> node.
            for (int j = 0; j < treatmentNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(treatmentNodes, j);
                mapObservation(resources, pertinentCRETypeCode, pertinentCRETypeDisplay, node);
            }
        }
        return resources;
    }

    /**
     * Create new Observation JSON object, and set attributes accordingly.
     * @param resources
     * @param pertinentCRETypeCode Category Code
     * @param pertinentCRETypeDisplay Category Display
     * @param node
     */
    private void mapObservation(List<Resource> resources, String pertinentCRETypeCode, String pertinentCRETypeDisplay, Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        var observation = new Observation();
        var entryId = entry.getId();
        observation.setId(entryId);
        observation.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_META));
        observation.addIdentifier().setValue(entryId);
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.addCategory(new CodeableConcept(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(pertinentCRETypeCode)
            .setDisplay(pertinentCRETypeDisplay)));
        observation.setCode(new CodeableConcept().addCoding(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(entry.getCodeValue())
            .setDisplay(entry.getCodeDisplay()))
        );
        entry.getEffectiveTimeLow().ifPresent(observation::setEffective);
        resources.add(observation);
    }
}
