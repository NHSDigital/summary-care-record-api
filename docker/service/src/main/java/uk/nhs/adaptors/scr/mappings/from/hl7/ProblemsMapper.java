package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
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

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.SNOMED_SYSTEM;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProblemsMapper {
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[.//UKCT_MT144038UK02.Problem]";
    private static final String PROBLEM_BASE_PATH = "./component/UKCT_MT144038UK02.Problem";
    private static final String UK_CORE_CONDITION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Condition";

    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    public List<? extends Resource>  map(Element document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList problemNodes = xmlUtils.getNodeListByXPath(pertinentCREType, PROBLEM_BASE_PATH);
            var pertinentCRETypeCode = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            for (int j = 0; j < problemNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(problemNodes, j);
                mapCondition(resources, pertinentCRETypeCode, pertinentCRETypeDisplay, node);
            }
        }

        return resources;
    }

    private void mapCondition(List<Resource> resources, String creTypeCode, String creTypeDisplay,  Node node) {
        var problem = new Condition();

        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

        problem.setId(entry.getId());

        if (entry.getEffectiveTimeLow().isPresent()) {
            problem.setOnset(entry.getEffectiveTimeLow().get());
        }

        problem.setMeta(new Meta().addProfile(UK_CORE_CONDITION_META));

        problem.setCode(new CodeableConcept().addCoding(new Coding()
            .setCode(entry.getCodeValue())
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(entry.getCodeDisplay())));

        List<CodeableConcept> category = new ArrayList<>();
        category.add(new CodeableConcept().addCoding(new Coding()
            .setCode(creTypeCode)
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(creTypeDisplay)));
        problem.setCategory(category);

        resources.add(problem);
    }
}
