package uk.nhs.adaptors.scr.mappings.from.hl7;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntry;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import static org.hl7.fhir.r4.model.Encounter.EncounterStatus.FINISHED;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.parseDate;

/**
 * Maps the Lifestyles from HL7 XML to FHIR JSON.
 * Closely related to Social or Personal Circumstances.
 * File & class name pluralised to avoid confusion/conflicts with mappings.from fhir.
 *
 * CMET: UKCT_MT144036UK01
 * SNOMED: 163021000000107
 * @see: NIAD-2324 and NIAD-2325
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LifestylesMapper implements XmlToFhirMapper {
    private final UuidWrapper uuid;
    private final ParticipantMapper participantMapper;
    private final CodedEntryMapper codedEntryMapper;
    private final XmlUtils xmlUtils;
    private static final String OBSERVATION_VALUE = "./value";
    private static final String PERTINENT_CODE_CODE_XPATH = "./code/@code";
    private static final String PERTINENT_CODE_DISPLAY_XPATH = "./code/@displayName";
    private static final String EFFECTIVE_TIME_CENTRE_XPATH = "./effectiveTime/centre/@value";
    private static final String PERTINENT_CRET_BASE_PATH = "/pertinentInformation2/pertinentCREType[."
        + "//UKCT_MT144036UK01.LifeStyle]";
    private static final String TREATMENTS_BASE_PATH = "./component/UKCT_MT144036UK01.LifeStyle";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String AUTHOR_XPATH = "./author";
    private static final String INFORMANT_XPATH = "./informant";
    private static final String PARTICIPANT_TIME_XPATH = "./time/@value";
    private static final String ENCOUNTER_PARTICIPATION_CODE_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";

    /**
     * List through all XML pertinent nodes (those following CRET path <UKCR...>).
     * Detach those XML nodes and run mapObservation with them.
     *
     * @param document
     * @return
     */
    public List<Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        NodeList pertinentNodes = xmlUtils.getNodeListByXPath(document, PERTINENT_CRET_BASE_PATH);
        // Loop through <pertinentCREType> node.
        for (int i = 0; i < pertinentNodes.getLength(); i++) {
            Node pertinentCREType = xmlUtils.getNodeAndDetachFromParent(pertinentNodes, i);
            NodeList treatmentNodes = xmlUtils.getNodeListByXPath(pertinentCREType, TREATMENTS_BASE_PATH);
            // Get "category" code/display values.
            var pertinentCRETypeCode = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_CODE_XPATH);
            var pertinentCRETypeDisplay = xmlUtils.getValueByXPath(pertinentCREType, PERTINENT_CODE_DISPLAY_XPATH);
            // Loop through <UKCT..> node.
            for (int j = 0; j < treatmentNodes.getLength(); j++) {
                Node node = xmlUtils.getNodeAndDetachFromParent(treatmentNodes, j);
                mapObservation(resources, pertinentCRETypeCode, pertinentCRETypeDisplay, node);
            }
        }

        // Return final FHIR-JSON.
        return resources;
    }

    /**
     * Create new Observation JSON object, and set attributes accordingly.
     * @param resources
     * @param pertinentCRETypeCode Category Code
     * @param pertinentCRETypeDisplay Category Display
     * @param node
     */
    private void mapObservation(List<Resource> resources, String pertinentCRETypeCode, String pertinentCRETypeDisplay, Node node) {
        CodedEntry entry = codedEntryMapper.getCommonCodedEntryValues(node);

        var entryId = entry.getId();
        var observation = new Observation();
        observation.setId(entryId);
        observation.setMeta(new Meta().addProfile(UK_CORE_PROCEDURE_META));
        observation.addIdentifier().setValue(entryId);
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.addCategory(new CodeableConcept(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(pertinentCRETypeCode)
            .setDisplay(pertinentCRETypeDisplay)));
        observation.setCode(new CodeableConcept().addCoding(new Coding()
            .setSystem(SNOMED_SYSTEM)
            .setCode(entry.getCodeValue())
            .setDisplay(entry.getCodeDisplay()))
        );

        // Get the value node if present. This will come through as Optional String, so
        // convert this to String, then String Type, to setValue.
        var observationValue = xmlUtils.getOptionalValueByXPath(node, OBSERVATION_VALUE);
        if (observationValue.isPresent()) {
            var observationValueStringType = new StringType(observationValue.map(Object::toString).orElse(null));
            observation.setValue(observationValueStringType);
        }

        // Retrieve and set effective time.
        if (entry.getEffectiveTimeLow().isPresent() || entry.getEffectiveTimeHigh().isPresent()) {
            var period = new Period();
            entry.getEffectiveTimeLow().ifPresent(period::setStartElement);
            entry.getEffectiveTimeHigh().ifPresent(period::setEndElement);
            observation.setEffective(period);
        } else {
            var effectiveTimeCentre =
                xmlUtils.getOptionalValueByXPath(node, EFFECTIVE_TIME_CENTRE_XPATH).map(it -> parseDate(it, DateTimeType.class));
            effectiveTimeCentre.ifPresent(observation::setEffective);
        }

        // Add observation to final FHIR-JSON.
        resources.add(observation);

        // Add encounter (Author/Informant) to FHIR-JSON.
        mapEncounter(node, observation, resources);
    }

    private void mapEncounter(Node node, Observation observation, List<Resource> resources) {
        Optional<Node> author = xmlUtils.detachOptionalNodeByXPath(node, AUTHOR_XPATH);
        Optional<Node> informant = xmlUtils.detachOptionalNodeByXPath(node, INFORMANT_XPATH);

        if (author.isPresent() || informant.isPresent()) {
            Encounter encounter = new Encounter();
            encounter.setStatus(FINISHED);
            encounter.setClass_(new Coding()
                .setCode("UNK")
                .setSystem("http://terminology.hl7.org/CodeSystem/v3-NullFlavor")
                .setDisplay("Unknown"));
            encounter.setId(uuid.randomUuid());
            author.ifPresent(authorNode -> mapAuthor(resources, encounter, authorNode));
            informant.ifPresent(informantNode -> mapInformant(resources, encounter, informantNode));
            observation.setEncounter(new Reference(encounter));
            resources.add(encounter);
        }
    }

    private void mapAuthor(List<Resource> resources, Encounter encounter, Node author) {
        DateTimeType time = parseDate(xmlUtils.getValueByXPath(author, PARTICIPANT_TIME_XPATH), DateTimeType.class);

        participantMapper.map(author)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole)
            .map(Reference::new)
            .forEach(it -> encounter.addParticipant(new Encounter.EncounterParticipantComponent()
                .setPeriod(new Period().setStartElement(time))
                .addType(getParticipationType("AUT", "author"))
                .setIndividual((it))));
    }

    private void mapInformant(List<Resource> resources, Encounter encounter, Node informant) {
        DateTimeType time = parseDate(xmlUtils.getValueByXPath(informant, PARTICIPANT_TIME_XPATH), DateTimeType.class);
        participantMapper.map(informant)
            .stream()
            .peek(it -> resources.add(it))
            .filter(it -> it instanceof PractitionerRole || it instanceof RelatedPerson)
            .forEach(it -> encounter.addParticipant(new Encounter.EncounterParticipantComponent()
                .setPeriod(new Period().setStartElement(time))
                .addType(getParticipationType("INF", "informant"))
                .setIndividual(new Reference(it))));
    }

    private CodeableConcept getParticipationType(String inf, String informant) {
        return new CodeableConcept(new Coding()
            .setCode(inf)
            .setSystem(ENCOUNTER_PARTICIPATION_CODE_SYSTEM)
            .setDisplay(informant));
    }
}
