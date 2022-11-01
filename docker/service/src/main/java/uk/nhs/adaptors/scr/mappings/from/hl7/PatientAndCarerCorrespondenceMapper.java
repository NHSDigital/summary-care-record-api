package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
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
public class PatientAndCarerCorrespondenceMapper implements XmlToFhirMapper {

    private final UuidWrapper uuid;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[.//UKCT_MT144035UK01.PatientCarerCorrespondence]";
    private static final String PATIENT_CARER__BASE_PATH = "./component/UKCT_MT144035UK01.PatientCarerCorrespondence";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";

    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList treatmentNodes = xmlUtils.getNodeListByXPath(pertinentCREType, PATIENT_CARER__BASE_PATH);
            for (int j = 0; j < treatmentNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(treatmentNodes, j);
                mapCommunication(resources, node);
            }
        }
        return resources;
    }

    private void mapCommunication(List<Resource> resources, Node node) {
        var communication = new Communication();

        communication.setId(uuid.randomUuid());
        communication.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_META));
        communication.setStatus(Communication.CommunicationStatus.COMPLETED);

        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

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
