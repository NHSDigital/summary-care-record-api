package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Meta;
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

import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.SNOMED_SYSTEM;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProblemsMapper {
    private static final String DIAGNOSIS_CODE = "163001000000103";
    private static final String DIAGNOSIS_DISPLAY = "Diagnoses";
    private static final String ALLERGY_CODE = "";
    private static final String ALLERGY_DISPLAY = "";
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;
    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[.//UKCT_MT144038UK02.Problem]";
    private static final String PROBLEM_BASE_PATH = "./component/UKCT_MT144038UK02.Problem";
    private static final String PROBLEM_ALLERGY_XPATH = "./pertinentInformation/AllergiesAndAdverseReactionsIdent";
    private static final String PROBLEM_DIAGNOSIS_XPATH = "./pertinentInformation/Diagnosis";
    private static final String CLINICAL_SYSTEM = "http://hl7.org/fhir/ValueSet/condition-clinical";
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

        problem.setMeta(new Meta().addProfile(UK_CORE_CONDITION_META));

        problem.addIdentifier().setValue(entry.getId());

        setClinicalStatus(problem, entry.getStatus());

        List<CodeableConcept> category = new ArrayList<>();
        category.add(new CodeableConcept().addCoding(new Coding()
            .setCode(creTypeCode)
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(creTypeDisplay)));
        problem.setCategory(category);

        problem.setCode(new CodeableConcept().addCoding(new Coding()
            .setCode(entry.getCodeValue())
            .setSystem(SNOMED_SYSTEM)
            .setDisplay(entry.getCodeDisplay())));

        if (entry.getEffectiveTimeLow().isPresent()) {
            problem.setOnset(entry.getEffectiveTimeLow().get());
        }

        // Commented out, awaiting further information and action in NIAD-2505
        // mapObservation(node, problem, resources);

        resources.add(problem);
    }

//    private void mapObservation(Node problem, Condition condition, List<Resource> resources) {
//        Optional<Node> allergies = xmlUtils.detachOptionalNodeByXPath(problem, PROBLEM_ALLERGY_XPATH);
//        if (allergies.isPresent()) {
//            mapAllergies(allergies.get(), condition);
//        }
//
//        Optional<Node> diagnosis = xmlUtils.detachOptionalNodeByXPath(problem, PROBLEM_DIAGNOSIS_XPATH);
//        if (diagnosis.isPresent()) {
//            mapDiagnosis(diagnosis.get(), condition);
//        }
//    }

//    private void mapAllergies(Node node, Condition condition) {
//        CodedEntry entry = codedEntryMapper.getEssentialCodedEntryValues(node);
//
//        var observation = new Observation();
//        observation.setId(entry.getId());
//        observation.setCode(new CodeableConcept().addCoding(new Coding()
//            .setCode(ALLERGY_CODE)
//            .setSystem(SNOMED_SYSTEM)
//            .setDisplay(ALLERGY_DISPLAY)));
//
//        var stage = new Condition.ConditionStageComponent();
//        stage.addAssessment(new Reference(observation));
//        condition.addStage(stage);
//    }

//    private void mapDiagnosis(Node node, Condition condition) {
//        CodedEntry entry = codedEntryMapper.getEssentialCodedEntryValues(node);
//
//        var observation = new Observation();
//        observation.setId(entry.getId());
//        observation.setCode(new CodeableConcept().addCoding(new Coding()
//            .setCode(DIAGNOSIS_CODE)
//            .setSystem(SNOMED_SYSTEM)
//            .setDisplay(DIAGNOSIS_DISPLAY)));
//
//        var stage = new Condition.ConditionStageComponent();
//        stage.addAssessment(new Reference(observation));
//        condition.addStage(stage);
//    }

    private static void setClinicalStatus(Condition condition, String value) {
        condition.setClinicalStatus(new CodeableConcept().addCoding(new Coding()
            .setSystem(CLINICAL_SYSTEM)
            .setCode(value)
            .setDisplay(StringUtils.capitalize(value))));
    }
}
