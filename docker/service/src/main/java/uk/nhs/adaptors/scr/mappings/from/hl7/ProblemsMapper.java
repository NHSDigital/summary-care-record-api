package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
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
public class ProblemsMapper {
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[.//UKCT_MT144038UK02.Problem]";
    private static final String PROBLEM_BASE_PATH = "./component/UKCT_MT144038UK02.Problem";
    private static final String UK_CORE_CONDITION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Condition";

    public List<? extends Resource>  map(Element document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList problemNodes = xmlUtils.getNodeListByXPath(pertinentCREType, PROBLEM_BASE_PATH);
            for (int j = 0; j < problemNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(problemNodes, j);
                mapCondition(resources, node);
            }
        }

        return resources;
    }

    private void mapCondition(List<Resource> resources, Node node) {
        var problem = new Condition();

        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

        problem.setId(entry.getId());

        resources.add(problem);
    }
}
