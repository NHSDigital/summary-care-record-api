package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from HL7 to FHIR for Patient or carer correspondence.
 * Despite having the same CMET number, Third party correspondence, and Care professional documentation are
 * mapped separately.
 *
 * CMET: UKCT_MT144035UK01
 */
public class PatientAndCarerCorrespondenceMapper implements XmlToFhirMapper {

    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType"
        + "[.//UKCT_MT144035UK01.PatientCarerCorrespondence]";
    private static final String PATIENT_CARER_BASE_PATH = "./component/UKCT_MT144035UK01.PatientCarerCorrespondence";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";

    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList treatmentNodes = xmlUtils.getNodeListByXPath(pertinentCREType, PATIENT_CARER_BASE_PATH);
            // Get "category" code/display values.
            var pertinentCRETypeCode = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            for (int j = 0; j < treatmentNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(treatmentNodes, j);
                mapCommunication(resources, pertinentCRETypeCode, pertinentCRETypeDisplay, node);
            }
        }
        return resources;
    }

    private void mapCommunication(List<Resource> resources, String pertinentCRETypeCode, String pertinentCRETypeDisplay,  Node node) {
        var communication = new Communication();
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        var entryId = entry.getId();
        communication.setId(entryId);
        communication.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_META));
        communication.addIdentifier().setValue(entryId);
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);
        communication.addCategory(new CodeableConcept(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(pertinentCRETypeCode)
            .setDisplay(pertinentCRETypeDisplay)));

        var topicCoding = new CodeableConcept().addCoding(new Coding()
            .setCode(entry.getCodeValue())
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(entry.getCodeDisplay()));
        communication.setTopic(topicCoding);

        if (entry.getEffectiveTimeLow().isPresent()) {
            communication.setSent(entry.getEffectiveTimeLow().get().getValue());
        }
        resources.add(communication);
    }
}
