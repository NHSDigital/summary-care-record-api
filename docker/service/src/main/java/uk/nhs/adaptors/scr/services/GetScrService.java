package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.spine.SpineClientContract;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.clients.SpineHttpClient;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.mappings.from.hl7.CareEventMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.DiagnosisMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.FindingMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.GpSummaryMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.InteractionMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.InvestigationMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.PatientCarerCorrespondenceMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.PersonalPreferenceMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.ProvisionOfAdviceAndInformationMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.RecordTargetMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.RiskToPatientMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.TreatmentMapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.XmlToFhirMapper;
import uk.nhs.adaptors.scr.models.EventListQueryParams;
import uk.nhs.adaptors.scr.models.EventListQueryResponse;
import uk.nhs.adaptors.scr.models.EventQueryParams;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;
import static org.hl7.fhir.r4.model.Bundle.SearchEntryMode.MATCH;
import static org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus.CURRENT;
import static uk.nhs.adaptors.scr.models.AcsPermission.ASK;
import static uk.nhs.adaptors.scr.models.AcsPermission.YES;
import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;
import static uk.nhs.adaptors.scr.utils.TemplateUtils.loadTemplate;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GetScrService {

    private static final Mustache QUPC_IN180000SM04_TEMPLATE = loadTemplate("QUPC_IN180000SM04.mustache");
    private static final Mustache QUPC_IN190000UK04_TEMPLATE = loadTemplate("QUPC_IN190000UK04.mustache");
    private static final String CORRELATION_ID_MDC_KEY = "CorrelationId";

    private static final String ACS_SYSTEM = "https://fhir.nhs.uk/CodeSystem/SCR-ACSPermission";
    private static final String NHS_ID_SYSTEM = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String SCR_ID_SYSTEM = "https://fhir.nhs.uk/Id/nhsSCRUUID";
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";
    private static final String GP_SUMMARY_SNOMED_CODE = "196981000000101";
    private static final String GP_SUMMARY_DISPLAY = " General Practice Summary";
    private static final String ATTACHMENT_URL = "%s/Bundle?composition.identifier=%s"
        + "$composition.subject:Patient.identifier=https://fhir.nhs.uk/Id/nhs-number|%s";

    private static final CodeableConcept GP_SUMMARY_SNOMED = new CodeableConcept(new Coding()
        .setSystem(SNOMED_SYSTEM)
        .setCode(GP_SUMMARY_SNOMED_CODE)
        .setDisplay(GP_SUMMARY_DISPLAY));

    private final SpineClientContract spineClient;

    private final ScrConfiguration scrConfiguration;
    private final SpineConfiguration spineConfiguration;

    private final InteractionMapper interactionMapper;
    private final GpSummaryMapper gpSummaryMapper;
    private final DiagnosisMapper diagnosisMapper;
    private final FindingMapper findingMapper;
    private final RiskToPatientMapper riskToPatientMapper;
    private final CareEventMapper careEventMapper;
    private final InvestigationMapper investigationMapper;
    private final TreatmentMapper treatmentMapper;
    private final PersonalPreferenceMapper personalPreferenceMapper;
    private final RecordTargetMapper recordTargetMapper;
    private final ProvisionOfAdviceAndInformationMapper adviceMapper;
    private final PatientCarerCorrespondenceMapper correspondenceMapper;

    public Bundle getScrId(String nhsNumber, String nhsdAsid, String clientIp) {
        String scrIdXml = getScrIdRawXml(nhsNumber, nhsdAsid, clientIp);

        EventListQueryResponse response = EventListQueryResponse.parseXml(scrIdXml);

        Bundle bundle = buildBundle();
        if (isPermissionGiven(response)) {
            bundle.setTotal(1);

            Patient patient = buildPatientResource(nhsNumber);
            DocumentReference documentReference = buildDocumentReference(nhsNumber, response, patient);

            bundle.addEntry(new BundleEntryComponent()
                .setFullUrl(getScrUrl() + "/DocumentReference/" + documentReference.getId())
                .setResource(documentReference)
                .setSearch(new Bundle.BundleEntrySearchComponent().setMode(MATCH))
            );

            bundle.addEntry(new BundleEntryComponent()
                .setFullUrl(patient.getId())
                .setResource(patient)
            );

        } else {
            bundle.setTotal(0);
        }

        return bundle;
    }

    private Bundle buildBundle() {
        Bundle bundle = new Bundle();
        bundle.setType(SEARCHSET);
        bundle.setId(randomUUID());
        return bundle;
    }

    public Bundle getScr(String nhsNumber, String nhsdAsid, String clientIp, String baseUrl) {
        String scrIdXml = getScrIdRawXml(nhsNumber, nhsdAsid, clientIp);
        LOGGER.debug("Received SCR ID XML:\n{}", scrIdXml);
        EventListQueryResponse response = EventListQueryResponse.parseXml(scrIdXml);

        if (isPermissionGiven(response)) {
            String psisEventId = response.getLatestScrId();
            String scrXml = getScrRawXml(psisEventId, nhsNumber, nhsdAsid, clientIp);
            LOGGER.debug("Received SCR XML:\n{}", scrXml);
            var document = parseDocument(scrXml);

            var bundle = interactionMapper.map(document);
            Patient patient = recordTargetMapper.mapPatient(document);

            Stream.<XmlToFhirMapper>of(
                gpSummaryMapper,
                diagnosisMapper,
                findingMapper,
                riskToPatientMapper,
                careEventMapper,
                investigationMapper,
                treatmentMapper,
                personalPreferenceMapper,
                adviceMapper,
                correspondenceMapper)
                .map(mapper -> mapper.map(document))
                .flatMap(resources -> resources.stream())
                .peek(it -> setPatientReferences(it, patient))
                .map(resource -> getBundleEntryComponent(baseUrl, resource))
                .forEach(bundle::addEntry);

            bundle.addEntry(getBundleEntryComponent(baseUrl, patient));
            bundle.setTotal(bundle.getEntry().size());

            //TODO list all Composition.section[].title and search xml using xpath to find all coded entries IDs and put in Composition.section.entry[]

            return bundle;

        } else {
            return interactionMapper.mapToEmpty();
        }
    }

    private BundleEntryComponent getBundleEntryComponent(String baseUrl, Resource resource) {
        return new BundleEntryComponent()
            .setFullUrl(baseUrl + "/" + resource.getResourceType() + "/" + resource.getId())
            .setResource(resource);
    }

    private void setPatientReferences(Resource resource, Patient patient) {
        if (resource instanceof ImmunizationRecommendation) {
            ImmunizationRecommendation recommendation = (ImmunizationRecommendation) resource;
            recommendation.setPatient(new Reference(patient));
        } else if (resource instanceof Immunization) {
            Immunization immunization = (Immunization) resource;
            immunization.setPatient(new Reference(patient));
        } else if (resource instanceof RelatedPerson) {
            RelatedPerson relatedPerson = (RelatedPerson) resource;
            relatedPerson.setPatient(new Reference(patient));
        } else if (resource instanceof Composition) {
            Composition composition = (Composition) resource;
            composition.setSubject(new Reference(patient));
        }
    }

    @SneakyThrows
    private static Document parseDocument(String xml) {
        return DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }

    private static boolean isPermissionGiven(EventListQueryResponse response) {
        return isNotEmpty(response.getLatestScrId()) && asList(YES, ASK).contains(response.getViewPermission());
    }

    private String getScrIdRawXml(String nhsNumber, String nhsdAsid, String clientIp) {
        String requestBody = prepareEventListQueryRequest(nhsNumber, nhsdAsid, clientIp);
        SpineHttpClient.Response result = spineClient.sendGetScrId(requestBody, nhsdAsid);
        return result.getBody();
    }

    private String getScrRawXml(String psisEventId, String nhsNumber, String nhsdAsid, String clientIp) {
        String requestBody = prepareEventQueryRequest(psisEventId, nhsNumber, nhsdAsid, clientIp);
        SpineHttpClient.Response result = spineClient.sendGetScr(requestBody, nhsdAsid);
        return result.getBody();
    }

    private DocumentReference buildDocumentReference(String nhsNumber,
                                                     EventListQueryResponse response,
                                                     Patient patient) {
        DocumentReference documentReference = new DocumentReference();
        documentReference.setId(randomUUID());

        documentReference.addSecurityLabel(new CodeableConcept(new Coding()
            .setCode(response.getViewPermission().getFhirValue())
            .setSystem(ACS_SYSTEM)));

        documentReference.setStatus(CURRENT);
        documentReference.setType(GP_SUMMARY_SNOMED);
        documentReference.setSubject(new Reference(patient));

        DocumentReferenceContentComponent content =
            buildDocumentReferenceContent(nhsNumber, response.getLatestScrId());
        documentReference.addContent(content);

        documentReference.setMasterIdentifier(new Identifier()
            .setValue(response.getLatestScrId())
            .setSystem(SCR_ID_SYSTEM));

        DocumentReferenceContextComponent context = new DocumentReferenceContextComponent();
        context.addEvent(GP_SUMMARY_SNOMED);
        documentReference.setContext(context);

        return documentReference;
    }

    private DocumentReferenceContentComponent buildDocumentReferenceContent(String nhsNumber, String scrId) {
        DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
        String attachmentUrl = String.format(ATTACHMENT_URL, getScrUrl(), scrId, nhsNumber);
        content.setAttachment(new Attachment().setUrl(attachmentUrl));
        return content;
    }


    private Patient buildPatientResource(String nhsNumber) {
        Patient patient = new Patient();
        String patientResourceId = randomUUID();
        patient.setId(patientResourceId);
        patient.setIdentifier(asList(new Identifier()
            .setSystem(NHS_ID_SYSTEM)
            .setValue(nhsNumber)));

        return patient;
    }

    private String prepareEventListQueryRequest(String nhsNumber, String nhsdAsid, String clientIp) {
        EventListQueryParams eventListQueryParams = new EventListQueryParams()
            .setGeneratedMessageId(MDC.get(CORRELATION_ID_MDC_KEY))
            .setMessageCreationTime(DateTimeFormatter.ofPattern("YYYYMMddHHmmss").format(now(UTC)))
            .setNhsNumber(nhsNumber)
            .setSenderFromASID(nhsdAsid)
            .setSpineToASID(scrConfiguration.getNhsdAsidTo())
            .setSpinePsisEndpointUrl(spineConfiguration.getUrl() + spineConfiguration.getPsisQueriesEndpoint())
            .setSenderHostIpAddress(clientIp);
        return TemplateUtils.fillTemplate(QUPC_IN180000SM04_TEMPLATE, eventListQueryParams);
    }

    private String prepareEventQueryRequest(String psisEventId, String nhsNumber, String nhsdAsid, String clientIp) {
        var eventListQueryParams = new EventQueryParams()
            .setGeneratedMessageId(MDC.get(CORRELATION_ID_MDC_KEY))
            .setMessageCreationTime(DateTimeFormatter.ofPattern("YYYYMMddHHmmss").format(now(UTC)))
            .setNhsNumber(nhsNumber)
            .setSenderFromASID(nhsdAsid)
            .setSpineToASID(scrConfiguration.getNhsdAsidTo())
            .setSpinePsisEndpointUrl(spineConfiguration.getUrl() + spineConfiguration.getPsisQueriesEndpoint())
            .setSenderHostIpAddress(clientIp)
            .setPsisEventId(psisEventId);
        return TemplateUtils.fillTemplate(QUPC_IN190000UK04_TEMPLATE, eventListQueryParams);
    }

    private String getScrUrl() {
        return String.format("%s/%s", scrConfiguration.getBaseUrl(), scrConfiguration.getServiceBasePath());
    }
}
