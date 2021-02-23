package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.ENTEREDINERROR;
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL;
import static uk.nhs.adaptors.scr.mappings.from.hl7.PerformerParticipationMode.getParticipationModeDisplay;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.parseDate;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getOptionalValueByXPath;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindingMapper implements XmlToFhirMapper {

    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String PERTINENT_CRET_BASE_PATH =
        GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType[.//UKCT_MT144043UK02.Finding]";
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String FINDING_BASE_PATH = "./component/UKCT_MT144043UK02.Finding";
    private static final String ENCOUNTER_PARTICIPATION_CODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";
    private static final String FINDING_AUTHOR_XPATH = "./author";
    private static final String FINDING_INFORMANT_XPATH = "./informant";
    private static final String FINDING_PERFORMER_XPATH = "./performer";
    private static final String FINDING_PARTICIPANT_TIME_XPATH = "./time/@value";
    private static final String FINDING_PERFORMER_MODE_CODE_XPATH = "./modeCode/@code";
    private static final String PERFORMER_EXTENSION_URL = "https://fhir.nhs.uk/StructureDefinition/Extension-SCR-ModeCode";
    private static final String ENCOUNTER_PARTICIPATION_MODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationMode";
    private static final String ENCOUNTER_CLASS_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-NullFlavor";
    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String EFFECTIVE_TIME_CENTRE_XPATH = "./effectiveTime/centre/@value";
    private static final String SARS_COV_2_CODE = "1240581000000104";

    private final ParticipantMapper participantMapper;
    private final CodedEntryMapper codedEntryMapper;

    @SneakyThrows
    public List<Resource> map(Node document) {
        var resources = new ArrayList<Resource>();
        for (var pertinentCREType : XmlUtils.getNodesByXPath(document, PERTINENT_CRET_BASE_PATH)) {
            var pertinentCRETypeCode = XmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = XmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            for (var node : XmlUtils.getNodesByXPath(pertinentCREType, FINDING_BASE_PATH)) {
                mapObservation(resources, pertinentCRETypeCode, pertinentCRETypeDisplay, node);
            }
        }
        return resources;
    }

    private void mapObservation(ArrayList<Resource> resources, String creTypeCode, String creTypeDisplay, Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        if (SARS_COV_2_CODE.equals(entry.getCodeValue())) {
            var effectiveTimeCentre =
                getOptionalValueByXPath(node, EFFECTIVE_TIME_CENTRE_XPATH).map(it -> parseDate(it, DateTimeType.class));

            var observation = new Observation();
            observation.setId(entry.getId());
            observation.setMeta(new Meta().addProfile(UK_CORE_OBSERVATION_META));
            observation.addIdentifier(new Identifier().setValue(entry.getId()));
            observation.setCode(new CodeableConcept().addCoding(new Coding()
                .setCode(entry.getCodeValue())
                .setSystem(SNOMED_SYSTEM)
                .setDisplay(entry.getCodeDisplay())));
            observation.setStatus(mapStatus(entry.getStatus()));

            if (entry.getEffectiveTimeLow().isPresent() || entry.getEffectiveTimeHigh().isPresent()) {
                var period = new Period();
                entry.getEffectiveTimeLow().ifPresent(period::setStartElement);
                entry.getEffectiveTimeHigh().ifPresent(period::setEndElement);
                observation.setEffective(period);
            } else {
                effectiveTimeCentre
                    .ifPresent(observation::setEffective);
            }

            observation.addCategory(new CodeableConcept(new Coding()
                .setSystem(SNOMED_SYSTEM)
                .setCode(creTypeCode)
                .setDisplay(creTypeDisplay)));

            mapEncounter(node, observation, resources);
            resources.add(observation);
        }
    }

    private static Observation.ObservationStatus mapStatus(String statusCode) {
        switch (statusCode) {
            case "normal":
            case "active":
            case "completed":
                return FINAL;
            case "nullified":
                return ENTEREDINERROR;
            default:
                throw new IllegalArgumentException(statusCode);
        }
    }

    private void mapEncounter(Node finding, Observation observation, List<Resource> resources) {
        Optional<Node> author = XmlUtils.getOptionalNodeByXpath(finding, FINDING_AUTHOR_XPATH);
        Optional<Node> informant = XmlUtils.getOptionalNodeByXpath(finding, FINDING_INFORMANT_XPATH);
        List<Node> performers = XmlUtils.getNodesByXPath(finding, FINDING_PERFORMER_XPATH);
        if (author.isPresent() || informant.isPresent() || !performers.isEmpty()) {
            Encounter encounter = new Encounter();
            encounter.setStatus(FINISHED);
            encounter.setClass_(new Coding()
                .setCode("UNK")
                .setSystem(ENCOUNTER_CLASS_SYSTEM)
                .setDisplay("Unknown"));
            encounter.setId(randomUUID());
            performers.stream().forEach(performerNode -> mapPerformer(resources, encounter, performerNode));
            author.ifPresent(authorNode -> mapAuthor(resources, encounter, authorNode));
            informant.ifPresent(informantNode -> mapInformant(resources, encounter, informantNode));
            observation.setEncounter(new Reference(encounter));
            resources.add(encounter);
        }
    }

    private void mapPerformer(List<Resource> resources, Encounter encounter, Node performer) {
        DateTimeType time = parseDate(XmlUtils.getValueByXPath(performer, FINDING_PARTICIPANT_TIME_XPATH), DateTimeType.class);
        participantMapper.map(performer)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole)
            .map(Reference::new)
            .forEach(it -> {
                EncounterParticipantComponent participant = new EncounterParticipantComponent();
                String modeCode = XmlUtils.getValueByXPath(performer, FINDING_PERFORMER_MODE_CODE_XPATH);
                participant.addExtension(PERFORMER_EXTENSION_URL, new CodeableConcept(
                    new Coding()
                        .setSystem(ENCOUNTER_PARTICIPATION_MODE_SYSTEM)
                        .setCode(modeCode)
                        .setDisplay(getParticipationModeDisplay(modeCode))));
                encounter.addParticipant(participant
                    .setPeriod(new Period().setStartElement(time))
                    .addType(getParticipationType("PRF", "performer"))
                    .setIndividual(it));
            });
    }

    private void mapInformant(List<Resource> resources, Encounter encounter, Node informant) {
        DateTimeType time = parseDate(XmlUtils.getValueByXPath(informant, FINDING_PARTICIPANT_TIME_XPATH), DateTimeType.class);
        participantMapper.map(informant)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole || it instanceof RelatedPerson)
            .map(Reference::new)
            .forEach(it -> encounter.addParticipant(new EncounterParticipantComponent()
                .setPeriod(new Period().setStartElement(time))
                .addType(getParticipationType("INF", "informant"))
                .setIndividual(it)));
    }

    private void mapAuthor(List<Resource> resources, Encounter encounter, Node author) {
        DateTimeType time = parseDate(XmlUtils.getValueByXPath(author, FINDING_PARTICIPANT_TIME_XPATH), DateTimeType.class);
        participantMapper.map(author)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole)
            .map(Reference::new)
            .forEach(it -> encounter.addParticipant(new EncounterParticipantComponent()
                .setPeriod(new Period().setStartElement(time))
                .addType(getParticipationType("AUT", "author"))
                .setIndividual(it)));
    }

    private CodeableConcept getParticipationType(String inf, String informant) {
        return new CodeableConcept(new Coding()
            .setCode(inf)
            .setSystem(ENCOUNTER_PARTICIPATION_CODE_SYSTEM)
            .setDisplay(informant));
    }
}
