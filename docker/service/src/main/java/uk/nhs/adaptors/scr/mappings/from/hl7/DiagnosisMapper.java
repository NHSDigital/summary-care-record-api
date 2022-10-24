package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.parseDate;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiagnosisMapper implements XmlToFhirMapper {

    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String PERTINENT_CRET_BASE_PATH =
        GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType[.//UKCT_MT144042UK01.Diagnosis]";

    private static final String DIAGNOSIS_BASE_PATH = "./component/UKCT_MT144042UK01.Diagnosis";

    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String DIAGNOSIS_PERTINENT_SUPPORTING_INFO_XPATH = "./pertinentInformation/pertinentSupportingInfo/value";
    private static final String DIAGNOSIS_PERTINENT_FINDINGS_XPATH = "./pertinentInformation1/pertinentFinding";
    private static final String DIAGNOSIS_PERTINENT_FINDING_ID_XPATH = "./id/@root";
    private static final String DIAGNOSIS_AUTHOR_XPATH = "./author";
    private static final String DIAGNOSIS_INFORMANT_XPATH = "./informant";
    private static final String DIAGNOSIS_PARTICIPANT_TIME_XPATH = "./time/@value";
    private static final String ENCOUNTER_PARTICIPATION_CODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";
    private static final List<String> COVID_19_ENTRIES = List.of("1240751000000100", "1300721000000109", "1300731000000106",
        "1240761000000102");
    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";

    private final ParticipantMapper participantMapper;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;

    @SneakyThrows
    public List<Resource> map(Node document) {
        var resources = new ArrayList<Resource>();
        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            var pertinentCRETypeCode = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            NodeList diagnosisNodes = xmlUtils.getNodeListByXPath(pertinentCREType, DIAGNOSIS_BASE_PATH);
            for (int j = 0; j < diagnosisNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(diagnosisNodes, j);
                CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
                if (COVID_19_ENTRIES.contains(entry.getCodeValue())) {
                    var pertinentSupportingInfo =
                        xmlUtils.getOptionalValueByXPath(node, DIAGNOSIS_PERTINENT_SUPPORTING_INFO_XPATH);

                    var condition = new Condition();
                    condition.setId(entry.getId());
                    condition.setMeta(new Meta().addProfile(UK_CORE_OBSERVATION_META));
                    condition.addIdentifier()
                        .setValue(entry.getId());
                    condition.setCode(new CodeableConcept().addCoding(new Coding()
                        .setCode(entry.getCodeValue())
                        .setSystem(SNOMED_SYSTEM)
                        .setDisplay(entry.getCodeDisplay())));
                    setConditionStatus(condition, entry.getStatus());

                    condition.addCategory(new CodeableConcept(new Coding()
                        .setSystem(SNOMED_SYSTEM)
                        .setCode(pertinentCRETypeCode)
                        .setDisplay(pertinentCRETypeDisplay)));

                    if (entry.getEffectiveTimeHigh().isPresent()) {
                        condition.setOnset(entry.getEffectiveTimeLow().get());
                        condition.setAbatement(entry.getEffectiveTimeHigh().get());
                    } else {
                        condition.setOnset(entry.getEffectiveTimeLow().get());
                    }

                    resources.add(condition);
                    pertinentSupportingInfo
                        .map(value -> new Annotation().setText(value))
                        .ifPresent(condition::addNote);

                    xmlUtils.getNodesByXPath(node, DIAGNOSIS_PERTINENT_FINDINGS_XPATH).stream()
                        .map(it -> xmlUtils.getValueByXPath(it, DIAGNOSIS_PERTINENT_FINDING_ID_XPATH))
                        .map(it -> new Reference(new Observation().setId(it)))
                        .map(reference -> new Condition.ConditionEvidenceComponent().addDetail(reference))
                        .forEach(condition::addEvidence);

                    mapEncounter(node, condition, resources);
                }
            }
        }
        return resources;
    }

    private void mapEncounter(Node diagnosis, Condition condition, List<Resource> resources) {
        Optional<Node> author = xmlUtils.detachOptionalNodeByXPath(diagnosis, DIAGNOSIS_AUTHOR_XPATH);
        Optional<Node> informant = xmlUtils.detachOptionalNodeByXPath(diagnosis, DIAGNOSIS_INFORMANT_XPATH);
        if (author.isPresent() || informant.isPresent()) {
            Encounter encounter = new Encounter();
            encounter.setStatus(FINISHED);
            encounter.setClass_(new Coding()
                .setCode("UNK")
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-NullFlavor")
                .setDisplay("Unknown"));
            encounter.setId(randomUUID());
            author.ifPresent(authorNode -> mapAuthor(resources, encounter, authorNode));
            informant.ifPresent(informantNode -> mapInformant(resources, encounter, informantNode));
            condition.setEncounter(new Reference(encounter));
            resources.add(encounter);
        }
    }

    private void mapInformant(List<Resource> resources, Encounter encounter, Node informant) {
        DateTimeType time = parseDate(xmlUtils.getValueByXPath(informant, DIAGNOSIS_PARTICIPANT_TIME_XPATH), DateTimeType.class);
        participantMapper.map(informant)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole || it instanceof RelatedPerson)
            .forEach(it -> encounter.addParticipant(new EncounterParticipantComponent()
                .setPeriod(new Period().setStartElement(time))
                .addType(getParticipationType("INF", "informant"))
                .setIndividual(new Reference(it))));
    }

    private void mapAuthor(List<Resource> resources, Encounter encounter, Node author) {
        DateTimeType time = parseDate(xmlUtils.getValueByXPath(author, DIAGNOSIS_PARTICIPANT_TIME_XPATH), DateTimeType.class);
        participantMapper.map(author)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole)
            .forEach(it -> encounter.addParticipant(new EncounterParticipantComponent()
                .setPeriod(new Period().setStartElement(time))
                .addType(getParticipationType("AUT", "author"))
                .setIndividual(new Reference(it))));
    }

    private CodeableConcept getParticipationType(String inf, String informant) {
        return new CodeableConcept(new Coding()
            .setCode(inf)
            .setSystem(ENCOUNTER_PARTICIPATION_CODE_SYSTEM)
            .setDisplay(informant));
    }

    private static void setConditionStatus(Condition condition, String statusCode) {
        switch (statusCode) {
            case "normal":
            case "active":
                setClinicalStatus(condition, "active");
                break;
            case "nullified":
                setVerificationStatus(condition, "entered-in-error");
                break;
            case "completed":
                setVerificationStatus(condition, "confirmed");
                break;
            default:
                throw new IllegalArgumentException(statusCode);
        }
    }

    private static void setClinicalStatus(Condition condition, String value) {
        condition.setClinicalStatus(new CodeableConcept().addCoding(new Coding()
            .setSystem("http://hl7.org/fhir/ValueSet/condition-clinical")
            .setCode(value)
            .setDisplay(StringUtils.capitalize(value))));
    }

    private static void setVerificationStatus(Condition condition, String value) {
        condition.setVerificationStatus(new CodeableConcept().addCoding(new Coding()
            .setSystem("http://hl7.org/fhir/ValueSet/condition-ver-status")
            .setCode(value)
            .setDisplay(StringUtils.capitalize(value))));
    }
}
