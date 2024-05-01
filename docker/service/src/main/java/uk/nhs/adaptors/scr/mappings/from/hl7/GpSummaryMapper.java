package uk.nhs.adaptors.scr.mappings.from.hl7;

import static org.hl7.fhir.r4.model.Composition.DocumentRelationshipType.REPLACES;

import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.parseDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;
import uk.nhs.adaptors.scr.utils.XmlUtils;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GpSummaryMapper implements XmlToFhirMapper {

    private static final String BASE_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String EVENT_ID_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject/queryResponseEvent/event/eventID/@root";
    private static final String GP_SUMMARY_ID_XPATH = BASE_XPATH + "/id/@root";
    private static final String GP_SUMMARY_CODE_CODE_XPATH = BASE_XPATH + "/code/@code";
    private static final String GP_SUMMARY_CODE_DISPLAY_NAME_XPATH = BASE_XPATH + "/code/@displayName";
    private static final String GP_SUMMARY_STATUS_CODE_XPATH = BASE_XPATH + "/statusCode/@code";
    private static final String GP_SUMMARY_EFFECTIVE_TIME_XPATH = BASE_XPATH + "/effectiveTime/@value";
    private static final String GP_SUMMARY_AUTHOR_TIME_XPATH = BASE_XPATH + "/author/time/@value";
    private static final String GP_SUMMARY_AUTHOR_AGENT_PERSON_SDS_XPATH = BASE_XPATH + "/author/UKCT_MT160018UK01.AgentPersonSDS";
    private static final String GP_SUMMARY_AUTHOR_AGENT_PERSON_XPATH = BASE_XPATH + "/author/UKCT_MT160018UK01.AgentPerson";
    private static final String GP_SUMMARY_AUTHOR_AGENT_ORG_SDS_XPATH =
            BASE_XPATH + "/author/UKCT_MT160017UK01.AgentOrgSDS/agentOrganizationSDS";
    private static final String GP_SUMMARY_AUTHOR_AGENT_ORG_XPATH = BASE_XPATH + "/author/UKCT_MT160017UK01.AgentOrg";
    private static final Map<String, String> CODED_ENTRY_RESOURCE_MAP = Map.of(
        "Clinical Observations and Findings", "Observation",
        "Investigation Results", "Observation",
        "Diagnoses", "Condition");

    private static final String REPLACEMENT_OF_PRIOR_MESSAGE_REF_ID_ROOT_XPATH =
            BASE_XPATH + "/replacementOf/priorMessageRef/id/@root";

    private static final String PERTINENT_ROOT_CRE_TYPE_CODE_CODE_XPATH =
            BASE_XPATH + "/pertinentInformation1/pertinentRootCREType/code/@code";
    private static final String PERTINENT_ROOT_CRE_TYPE_CODE_DISPLAY_NAME_XPATH =
            BASE_XPATH + "/pertinentInformation1/pertinentRootCREType/code/@displayName";

    private static final String PRESENTATION_TEXT_VALUE =
            BASE_XPATH + "/excerptFrom/UKCT_MT144051UK01.CareProfessionalDocumentationCRE/component/presentationText/value/html";

    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String PERTINENT_CRET_BASE_PATH =
            GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType[descendant::code[@displayName='Diagnoses' "
                    + "or @displayName='Investigation Results' "
                    + "or @displayName='Clinical Observations and Findings']]";

    private static final String COMPONENT_XPATH = "./component";
    private static final String COMPONENT_DISPLAY_XPATH = "./code/@displayName";
    private static final String CODED_ENTRY_ID_XPATH = "./*/id/@root";
    private static final String CODED_ENTRY_CODE_XPATH = "./*/code/@code";

    private static final List<String> COVID_19_CODES = List.of("1240751000000100", "1300721000000109", "1300731000000106",
            "1240761000000102", "1240581000000104");

    private final AgentPersonSdsMapper agentPersonSdsMapper;
    private final AgentPersonMapper agentPersonMapper;
    private final OrganisationSdsMapper organisationSdsMapper;
    private final AgentOrganisationMapper agentOrganisationMapper;
    private final HtmlParser htmlParser;
    private final XmlUtils xmlUtils;

    @SneakyThrows
    public List<Resource> map(Node document) {
        var gpSummaryId =
                xmlUtils.getValueByXPath(document, GP_SUMMARY_ID_XPATH);
        var gpSummaryCodeCode =
                xmlUtils.getValueByXPath(document, GP_SUMMARY_CODE_CODE_XPATH);
        var gpSummaryCodeDisplayName =
                xmlUtils.getValueByXPath(document, GP_SUMMARY_CODE_DISPLAY_NAME_XPATH);
        var gpSummaryStatusCode =
                xmlUtils.getValueByXPath(document, GP_SUMMARY_STATUS_CODE_XPATH);
        var gpSummaryEffectiveTime =
                parseDate(xmlUtils.getValueByXPath(document, GP_SUMMARY_EFFECTIVE_TIME_XPATH), InstantType.class);
        var authorTime =
                parseDate(xmlUtils.getValueByXPath(document, GP_SUMMARY_AUTHOR_TIME_XPATH), DateTimeType.class);
        var replacementOfPriorMessageRefIdRoot =
                xmlUtils.getOptionalValueByXPath(document, REPLACEMENT_OF_PRIOR_MESSAGE_REF_ID_ROOT_XPATH);
        var pertinentRootCreTypeCodeCode =
                xmlUtils.getValueByXPath(document, PERTINENT_ROOT_CRE_TYPE_CODE_CODE_XPATH);
        var pertinentRootCreTypeCodeDisplayName =
                xmlUtils.getValueByXPath(document, PERTINENT_ROOT_CRE_TYPE_CODE_DISPLAY_NAME_XPATH);
        var presentationTextValue =
                xmlUtils.detachOptionalNodeByXPath(document, PRESENTATION_TEXT_VALUE);
        var eventId = xmlUtils.getValueByXPath(document, EVENT_ID_XPATH);

        List<Resource> resources = new ArrayList<>();
        var composition = new Composition();
        composition.setId(eventId);

        composition.setIdentifier(
                new Identifier()
                        .setValue(gpSummaryId)
                        .setSystem("https://tools.ietf.org/html/rfc4122"));

        composition.setType(
                new CodeableConcept().addCoding(new Coding()
                        .setCode(gpSummaryCodeCode)
                        .setSystem(SNOMED_SYSTEM)
                        .setDisplay(gpSummaryCodeDisplayName)));

        composition.setMeta(new Meta().setLastUpdatedElement(gpSummaryEffectiveTime));

        composition.setStatus(
                mapCompositionStatus(gpSummaryStatusCode));

        composition.setDateElement(authorTime);

        replacementOfPriorMessageRefIdRoot
                .ifPresent(val -> composition.addRelatesTo(
                        new Composition.CompositionRelatesToComponent().setTarget(new Identifier()
                                .setValue(val))
                                .setCode(REPLACES)
                ));

        composition.addCategory(
                new CodeableConcept().addCoding(new Coding()
                        .setCode(pertinentRootCreTypeCodeCode)
                        .setSystem(SNOMED_SYSTEM)
                        .setDisplay(pertinentRootCreTypeCodeDisplayName)));

        Map<String, List<String>> references = sectionReferences(document);

        presentationTextValue
                .map(htmlParser::parse)
                .map(Collection::stream)
                .ifPresent(it -> it.forEach(section -> {
                    if (section.getTitle() != null
                            && CODED_ENTRY_RESOURCE_MAP.keySet().contains(section.getTitle())
                            && references.containsKey(section.getTitle())) {
                        for (String codedEntryId : references.get(section.getTitle())) {
                            section.addEntry(new Reference(CODED_ENTRY_RESOURCE_MAP.get(section.getTitle()) + "/" + codedEntryId));
                        }
                    }
                    composition.addSection(section);
                }));

        resources.add(composition);

        addAuthor(document, resources, composition);

        return resources;
    }

    private Map<String, List<String>> sectionReferences(Node document) {
        Map<String, List<String>> references = new HashMap<>();
        for (Node pertinentNode : xmlUtils.getNodesByXPath(document, PERTINENT_CRET_BASE_PATH)) {
            for (Node component : xmlUtils.getNodesByXPath(pertinentNode, COMPONENT_XPATH)) {
                String code = xmlUtils.getValueByXPath(component, CODED_ENTRY_CODE_XPATH);
                if (COVID_19_CODES.contains(code)) {
                    String display = xmlUtils.getValueByXPath(pertinentNode, COMPONENT_DISPLAY_XPATH);
                    String id = xmlUtils.getValueByXPath(component, CODED_ENTRY_ID_XPATH);
                    putToMap(references, display, id);
                }
            }
        }

        return references;
    }

    private static void putToMap(Map<String, List<String>> map, String key, String value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(value);
    }

    private void addAuthor(Node document, List<Resource> resources, Composition composition) {
        addAuthorPerson(document, resources, composition);
        addAuthorOrganisation(document, resources, composition);
    }

    private void addAuthorPerson(Node document, List<Resource> resources, Composition composition) {
        xmlUtils.detachOptionalNodeByXPath(document, GP_SUMMARY_AUTHOR_AGENT_PERSON_SDS_XPATH)
                .ifPresent(agentPersonSds -> {
                    List<? extends Resource> authorResources = agentPersonSdsMapper.map(agentPersonSds);
                    resources.addAll(authorResources);
                    composition.addAuthor(findPractitionerRole(authorResources));
                });

        xmlUtils.detachOptionalNodeByXPath(document, GP_SUMMARY_AUTHOR_AGENT_PERSON_XPATH)
                .ifPresent(agentPersonSds -> {
                    List<? extends Resource> authorResources = agentPersonMapper.map(agentPersonSds);
                    resources.addAll(authorResources);
                    composition.addAuthor(findPractitionerRole(authorResources));
                });
    }

    private void addAuthorOrganisation(Node document, List<Resource> resources, Composition composition) {
        xmlUtils.detachOptionalNodeByXPath(document, GP_SUMMARY_AUTHOR_AGENT_ORG_SDS_XPATH)
            .ifPresent(agentOrganisationSds -> {
                Organization organisation = organisationSdsMapper.mapOrganizationSds(agentOrganisationSds);
                resources.add(organisation);
                composition.addAuthor(new Reference(organisation));
            });

        xmlUtils.detachOptionalNodeByXPath(document, GP_SUMMARY_AUTHOR_AGENT_ORG_XPATH)
            .ifPresent(agentOrganisation -> {
                List<? extends Resource> organisationResources = agentOrganisationMapper.map(agentOrganisation);
                resources.addAll(organisationResources);
                composition.addAuthor(findPractitionerRole(organisationResources));
            });
    }

    private Reference findPractitionerRole(List<? extends Resource> authorResources) {

        if (!authorResources.isEmpty()) {
            return authorResources
                .stream()
                .filter(it -> it instanceof PractitionerRole)
                .map(Reference::new)
                .findFirst()
                .get();
        } else {
            throw new ScrBaseException("No practitioner role found in GpSummary Mapper");
        }
    }

    private static Composition.CompositionStatus mapCompositionStatus(String compositionStatus) {
        switch (compositionStatus) {
            case "active":
                return Composition.CompositionStatus.FINAL;
            default:
                throw new IllegalArgumentException(String.format("Unable to map '%s'", compositionStatus));
        }
    }
}
