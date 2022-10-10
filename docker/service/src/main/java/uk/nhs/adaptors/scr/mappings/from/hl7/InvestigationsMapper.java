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

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InvestigationsMapper implements XmlToFhirMapper {

    private final UuidWrapper uuid;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;
    private List<CodeableConcept> coding = new ArrayList<>();


    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

//        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
//        for (int i = 0; i < pertinentNodes.getLength(); i++) {
//            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
//            NodeList careEventNodes = xmlUtils.getNodeListByXPath(pertinentCREType, CARE_EVENT_BASE_PATH);
//            for (int j = 0; j < careEventNodes.getLength(); j++) {
//                Node node = xmlUtils.getNodeAndDetachFromParent(careEventNodes, j);
//                mapEncounter(resources, node);
//            }
//        }
        return resources;
    }

}
