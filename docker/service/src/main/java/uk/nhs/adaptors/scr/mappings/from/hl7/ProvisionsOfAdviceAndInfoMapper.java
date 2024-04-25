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
 * Maps the Provision Of Advice And Information from HL7 to FHIR.
 *
 * CMET: UKCT_MT144049UK01
 * Snomed: 163101000000102
 * @see: NIAD-2318
 */
public class ProvisionsOfAdviceAndInfoMapper implements XmlToFhirMapper {

    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[.//"
        + "UKCT_MT144049UK01.ProvisionOfAdviceAndInformation]";
    private static final String TREATMENTS_BASE_PATH = "./component/UKCT_MT144049UK01.ProvisionOfAdviceAndInformation";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";

    public List<Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList treatmentNodes = xmlUtils.getNodeListByXPath(pertinentCREType, TREATMENTS_BASE_PATH);
            for (int j = 0; j < treatmentNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(treatmentNodes, j);
                mapCommunication(resources, node);
            }
        }
        return resources;
    }

    private void mapCommunication(List<Resource> resources, Node node) {
        var communication = new Communication();
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        var entryId = entry.getId();
        communication.setId(entryId);
        communication.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_META));
        communication.addIdentifier().setValue(entryId);
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);

        var coding = new CodeableConcept().addCoding(new Coding()
            .setCode(entry.getCodeValue())
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(entry.getCodeDisplay()));
        communication.setTopic(coding);

        entry.getEffectiveTimeLow().ifPresent(communication::setSentElement);

        resources.add(communication);
    }
}
