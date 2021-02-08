package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent;
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
import uk.nhs.adaptors.scr.mappings.from.hl7.common.ObservationCommonMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;
import static org.hl7.fhir.r4.model.Immunization.ImmunizationStatus.NOTDONE;
import static uk.nhs.adaptors.scr.mappings.from.hl7.PerformerParticipationMode.getParticipationModeDisplay;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindingMapper implements XmlToFhirMapper {

    private static final String UK_CORE_IMMUNIZATION_PROFILE = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Immunization";
    private static final String IMMUNIZATION_EXTENSION_URL =
        "https://fhir.hl7.org.uk/StructureDefinition/Extension-UKCore-VaccinationProcedure";
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
    private static final List<String> SARS_COV_2_CODES = List.of("1240581000000104", "163131000000108");
    private static final String MEDICATION_RECOMMENDATION_CRE_TYPE = "185371000000109";
    private static final String MEDICATION_RECORD_CRE_TYPE = "163111000000100";
    private static final String INVESTIGATION_RESULT_CRE_TYPE = "163141000000104";
    private static final String CLINICAL_OBSERVATION_CRE_TYPE = "163131000000108";

    private final ParticipantMapper participantMapper;
    private final ObservationCommonMapper observationCommonMapper;
    private final CodedEntryMapper codedEntryMapper;

    @SneakyThrows
    public List<Resource> map(Node document) {
        var resources = new ArrayList<Resource>();
        for (var pertinentCREType : XmlUtils.getNodesByXPath(document, PERTINENT_CRET_BASE_PATH)) {
            var pertinentCRETypeCode = XmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = XmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            for (var node : XmlUtils.getNodesByXPath(pertinentCREType, FINDING_BASE_PATH)) {
                switch (pertinentCRETypeCode) {
                    case MEDICATION_RECOMMENDATION_CRE_TYPE:
                        resources.add(mapMedicationRecommendation(node));
                        break;
                    case MEDICATION_RECORD_CRE_TYPE:
                        resources.add(mapMedication(node));
                        break;
                    case CLINICAL_OBSERVATION_CRE_TYPE:
                    case INVESTIGATION_RESULT_CRE_TYPE:
                        resources.add(mapObservation(resources, pertinentCRETypeCode, pertinentCRETypeDisplay, node));
                }
            }
        }
        return resources;
    }

    private Immunization mapMedication(Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        Immunization immunization = new Immunization();
        immunization.setId(entry.getId());
        immunization.setMeta(new Meta().addProfile(UK_CORE_IMMUNIZATION_PROFILE));
        immunization.addExtension(new Extension()
            .setUrl(IMMUNIZATION_EXTENSION_URL)
            .setValue(new CodeableConcept(new Coding()
                .setSystem(SNOMED_SYSTEM)
                .setCode(entry.getCodeValue())
                .setDisplay(entry.getCodeDisplay()))
            )
        );
        immunization.setStatus(NOTDONE);
        immunization.setVaccineCode(new CodeableConcept(new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-NullFlavor")
                .setCode("UNK")
                .setDisplay("Unknown")
            )
        );
        entry.getEffectiveTimeLow()
            .ifPresent(date -> immunization.setOccurrence(new DateTimeType(date)));
        return immunization;
    }

    private ImmunizationRecommendation mapMedicationRecommendation(Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);
        ImmunizationRecommendation recommendation = new ImmunizationRecommendation();
        recommendation.setId(entry.getId());
        recommendation.setDate(entry.getEffectiveTimeLow().get());
        ImmunizationRecommendationRecommendationComponent component =
            new ImmunizationRecommendationRecommendationComponent();
        component.addContraindicatedVaccineCode().addCoding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(entry.getCodeValue())
            .setDisplay(entry.getCodeDisplay());
        recommendation.addRecommendation(component);

        return recommendation;
    }

    private Observation mapObservation(ArrayList<Resource> resources, String creTypeCode, String creTypeDisplay, Node node) {
        Observation observation = observationCommonMapper.mapObservation(node);

        observation.addCategory(new CodeableConcept(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(creTypeCode)
            .setDisplay(creTypeDisplay)));

        mapEncounter(node, observation, resources);

        return observation;
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
        Date time = XmlToFhirMapper.parseDate(XmlUtils.getValueByXPath(performer, FINDING_PARTICIPANT_TIME_XPATH));
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
                    .setPeriod(new Period().setStart(time))
                    .addType(getParticipationType("PRF", "performer"))
                    .setIndividual(it));
            });
    }

    private void mapInformant(List<Resource> resources, Encounter encounter, Node informant) {
        Date time = XmlToFhirMapper.parseDate(XmlUtils.getValueByXPath(informant, FINDING_PARTICIPANT_TIME_XPATH));
        participantMapper.map(informant)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole || it instanceof RelatedPerson)
            .map(Reference::new)
            .forEach(it -> encounter.addParticipant(new EncounterParticipantComponent()
                .setPeriod(new Period().setStart(time))
                .addType(getParticipationType("INF", "informant"))
                .setIndividual(it)));
    }

    private void mapAuthor(List<Resource> resources, Encounter encounter, Node author) {
        Date time = XmlToFhirMapper.parseDate(XmlUtils.getValueByXPath(author, FINDING_PARTICIPANT_TIME_XPATH));
        participantMapper.map(author)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole)
            .map(Reference::new)
            .forEach(it -> encounter.addParticipant(new EncounterParticipantComponent()
                .setPeriod(new Period().setStart(time))
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
