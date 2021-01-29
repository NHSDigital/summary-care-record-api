package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiagnosisMapper implements XmlToFhirMapper {

    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String DIAGNOSIS_BASE_PATH =
        GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType/component/UKCT_MT144042UK01.Diagnosis";

    private static final String DIAGNOSIS_ID_XPATH = "./id/@root";
    private static final String DIAGNOSIS_CODE_CODE_XPATH = "./code/@code";
    private static final String DIAGNOSIS_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String DIAGNOSIS_STATUS_CODE_CODE_XPATH = "./statusCode/@code";
    private static final String DIAGNOSIS_EFFECTIVE_TIME_LOW_XPATH = "./effectiveTime/low/@value";
    private static final String DIAGNOSIS_EFFECTIVE_TIME_HIGH_XPATH = "./effectiveTime/high/@value";
    private static final String DIAGNOSIS_PERTINENT_SUPPORTING_INFO_XPATH = "./pertinentInformation/pertinentSupportingInfo/value";
    private static final String DIAGNOSIS_PERTINENT_FINDING_XPATH = "./pertinentInformation1/templateId/pertinentFinding/id/@root";
    private static final String DIAGNOSIS_AUTHOR_XPATH = "./author";
    private static final String DIAGNOSIS_INFORMANT_XPATH = "./informant";
    private static final String DIAGNOSIS_PARTICIPANT_TIME_XPATH = "./time/@value";
    private static final String ENCOUNTER_PARTICIPATION_CODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";

    private final ParticipantMapper participantMapper;

    @SneakyThrows
    public List<Resource> map(Node document) {
        var resources = new ArrayList<Resource>();
        for (var node : XmlUtils.getNodesByXPath(document, DIAGNOSIS_BASE_PATH)) {
            var diagnosisId =
                XmlUtils.getValueByXPath(node, DIAGNOSIS_ID_XPATH);
            var diagnosisCodeCode =
                XmlUtils.getValueByXPath(node, DIAGNOSIS_CODE_CODE_XPATH);
            var diagnosisCodeDisplay =
                XmlUtils.getValueByXPath(node, DIAGNOSIS_CODE_DISPLAY_XPATH);
            var diagnosisStatusCodeCode =
                XmlUtils.getValueByXPath(node, DIAGNOSIS_STATUS_CODE_CODE_XPATH);
            var diagnosisEffectiveTimeLow =
                XmlToFhirMapper.parseDate(XmlUtils.getValueByXPath(node, DIAGNOSIS_EFFECTIVE_TIME_LOW_XPATH));
            var diagnosisEffectiveTimeHigh =
                XmlUtils.getOptionalValueByXPath(node, DIAGNOSIS_EFFECTIVE_TIME_HIGH_XPATH)
                    .map(XmlToFhirMapper::parseDate);
            var pertinentSupportingInfo =
                XmlUtils.getOptionalValueByXPath(node, DIAGNOSIS_PERTINENT_SUPPORTING_INFO_XPATH);
            var pertinentFinding =
                XmlUtils.getOptionalValueByXPath(node, DIAGNOSIS_PERTINENT_FINDING_XPATH);

            var condition = new Condition();
            condition.setId(randomUUID());
            condition.addIdentifier()
                .setValue(diagnosisId);
            condition.setCode(new CodeableConcept().addCoding(new Coding()
                .setCode(diagnosisCodeCode)
                .setSystem(SNOMED_SYSTEM)
                .setDisplay(diagnosisCodeDisplay)));
            setConditionStatus(condition, diagnosisStatusCodeCode);

            var lowDateTime = new DateTimeType();
            lowDateTime.setValue(diagnosisEffectiveTimeLow);
            if (diagnosisEffectiveTimeHigh.isPresent()) {
                condition.setOnset(lowDateTime);
                condition.setAbatement(new DateTimeType().setValue(diagnosisEffectiveTimeHigh.get()));
            } else {
                condition.setOnset(lowDateTime);
            }

            pertinentSupportingInfo
                .map(value -> new Annotation().setText(value))
                .ifPresent(condition::addNote);
            pertinentFinding
                .map(Reference::new)
                .map(reference -> new Condition.ConditionEvidenceComponent().addDetail(reference))
                .ifPresent(condition::addEvidence);

            resources.add(condition);
            mapEncounter(node, condition, resources);
        }
        return resources;
    }

    private void mapEncounter(Node diagnosis, Condition condition, List<Resource> resources) {
        Optional<Node> author = XmlUtils.getOptionalNodeByXpath(diagnosis, DIAGNOSIS_AUTHOR_XPATH);
        Optional<Node> informant = XmlUtils.getOptionalNodeByXpath(diagnosis, DIAGNOSIS_INFORMANT_XPATH);
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
        Date time = XmlToFhirMapper.parseDate(XmlUtils.getValueByXPath(informant, DIAGNOSIS_PARTICIPANT_TIME_XPATH));
        participantMapper.map(informant)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole || it instanceof RelatedPerson)
            .forEach(it -> encounter.addParticipant(new EncounterParticipantComponent()
                .setPeriod(new Period().setStart(time))
                .addType(getParticipationType("INF", "informant"))
                .setIndividual(new Reference(it))));
    }

    private void mapAuthor(List<Resource> resources, Encounter encounter, Node author) {
        Date time = XmlToFhirMapper.parseDate(XmlUtils.getValueByXPath(author, DIAGNOSIS_PARTICIPANT_TIME_XPATH));
        participantMapper.map(author)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole)
            .forEach(it -> encounter.addParticipant(new EncounterParticipantComponent()
                .setPeriod(new Period().setStart(time))
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
            .setCode(value)));
    }

    private static void setVerificationStatus(Condition condition, String value) {
        condition.setVerificationStatus(new CodeableConcept().addCoding(new Coding()
            .setSystem("http://hl7.org/fhir/ValueSet/condition-ver-status")
            .setCode(value)));
    }
}
