package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hl7.fhir.r4.model.Composition.DocumentRelationshipType.REPLACES;
import static uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper.parseDate;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResource;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GpSummaryMapper implements XmlToFhirMapper {

    private static final String BASE_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String EVENT_ID_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject/queryResponseEvent/event/eventID/@root";

    private static final String GP_SUMMARY_ID_XPATH = BASE_XPATH + "/id/@root";
    private static final String GP_SUMMARY_CODE_CODE_XPATH = BASE_XPATH + "/code/@code";
    private static final String GP_SUMMARY_CODE_CODE_SYSTEM_XPATH = BASE_XPATH + "/code/@codeSystem";
    private static final String GP_SUMMARY_CODE_DISPLAY_NAME_XPATH = BASE_XPATH + "/code/@displayName";
    private static final String GP_SUMMARY_STATUS_CODE_XPATH = BASE_XPATH + "/statusCode/@code";
    private static final String GP_SUMMARY_EFFECTIVE_TIME_XPATH = BASE_XPATH + "/effectiveTime/@value";
    private static final String GP_SUMMARY_AUTHOR_TIME_XPATH = BASE_XPATH + "/author/time/@value";
    private static final String GP_SUMMARY_AUTHOR_AGENT_PERSON_SDS_XPATH = BASE_XPATH + "/author/UKCT_MT160018UK01.AgentPersonSDS";
    private static final String GP_SUMMARY_AUTHOR_AGENT_PERSON_XPATH = BASE_XPATH + "/author/UKCT_MT160018UK01.AgentPerson";

    private static final String REPLACEMENT_OF_PRIOR_MESSAGE_REF_ID_ROOT_XPATH =
        BASE_XPATH + "/replacementOf/priorMessageRef/id/@root";

    private static final String PERTINENT_ROOT_CRE_TYPE_CODE_CODE_XPATH =
        BASE_XPATH + "/pertinentInformation1/pertinentRootCREType/code/@code";
    private static final String PERTINENT_ROOT_CRE_TYPE_CODE_CODE_SYSTEM_XPATH =
        BASE_XPATH + "/pertinentInformation1/pertinentRootCREType/code/@codeSystem";
    private static final String PERTINENT_ROOT_CRE_TYPE_CODE_DISPLAY_NAME_XPATH =
        BASE_XPATH + "/pertinentInformation1/pertinentRootCREType/code/@displayName";

    private static final String PRESENTATION_TEXT_VALUE =
        BASE_XPATH + "/excerptFrom/UKCT_MT144051UK01.CareProfessionalDocumentationCRE/component/presentationText/value/html";

    private static final String CODED_ENTRY_ID_XPATH =
        BASE_XPATH + "/pertinentInformation2/pertinentCREType/code[@displayName='%s']/following-sibling::component/*/id/@root";

    private final AgentPersonSdsMapper agentPersonSdsMapper;
    private final AgentPersonMapper agentPersonMapper;
    private final HtmlParser htmlParser;

    @SneakyThrows
    public List<Resource> map(Node document) {
        var gpSummaryId =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_ID_XPATH);
        var gpSummaryCodeCode =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_CODE_CODE_XPATH);
        var gpSummaryCodeDisplayName =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_CODE_DISPLAY_NAME_XPATH);
        var gpSummaryStatusCode =
            XmlUtils.getValueByXPath(document, GP_SUMMARY_STATUS_CODE_XPATH);
        var gpSummaryEffectiveTime =
            parseDate(XmlUtils.getValueByXPath(document, GP_SUMMARY_EFFECTIVE_TIME_XPATH), InstantType.class);
        var authorTime =
            parseDate(XmlUtils.getValueByXPath(document, GP_SUMMARY_AUTHOR_TIME_XPATH), DateTimeType.class);
        var replacementOfPriorMessageRefIdRoot =
            XmlUtils.getOptionalValueByXPath(document, REPLACEMENT_OF_PRIOR_MESSAGE_REF_ID_ROOT_XPATH);
        var pertinentRootCreTypeCodeCode =
            XmlUtils.getValueByXPath(document, PERTINENT_ROOT_CRE_TYPE_CODE_CODE_XPATH);
        var pertinentRootCreTypeCodeDisplayName =
            XmlUtils.getValueByXPath(document, PERTINENT_ROOT_CRE_TYPE_CODE_DISPLAY_NAME_XPATH);
        var presentationTextValue =
            XmlUtils.getNodesByXPath(document, PRESENTATION_TEXT_VALUE).stream()
                .findFirst();
        var eventId = XmlUtils.getValueByXPath(document, EVENT_ID_XPATH);

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

        presentationTextValue
            .map(htmlParser::parse)
            .map(Collection::stream)
            .ifPresent(section -> section.forEach(composition::addSection));

        resources.add(composition);

        addAuthor(document, resources, composition);

        return resources;
    }

    public void map(Bundle bundle, Document document) {
        for (var section : getDomainResource(bundle, Composition.class).getSection()) {
            var xpath = String.format(CODED_ENTRY_ID_XPATH, section.getTitle());
            for (Node node : XmlUtils.getNodesByXPath(document.getDocumentElement(), xpath)) {
                var codedEntryId = node.getNodeValue();
                section.addEntry(new Reference().setReference(codedEntryId));
            }
        }
    }

    private void addAuthor(Node document, List<Resource> resources, Composition composition) {
        XmlUtils.getOptionalNodeByXpath(document, GP_SUMMARY_AUTHOR_AGENT_PERSON_SDS_XPATH)
            .ifPresent(agentPersonSds -> {
                List<? extends Resource> authorResources = agentPersonSdsMapper.map(agentPersonSds);
                resources.addAll(authorResources);
                composition.addAuthor(findPractitionerRole(authorResources));
            });

        XmlUtils.getOptionalNodeByXpath(document, GP_SUMMARY_AUTHOR_AGENT_PERSON_XPATH)
            .ifPresent(agentPersonSds -> {
                List<? extends Resource> authorResources = agentPersonMapper.map(agentPersonSds);
                resources.addAll(authorResources);
                composition.addAuthor(findPractitionerRole(authorResources));
            });
    }


    private Reference findPractitionerRole(List<? extends Resource> authorResources) {
        return authorResources
            .stream()
            .filter(it -> it instanceof PractitionerRole)
            .map(Reference::new)
            .findFirst()
            .get();
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
