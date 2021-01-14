package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContextComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.spine.SpineClientContract;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.models.EventListQueryParams;
import uk.nhs.adaptors.scr.models.EventListQueryResponse;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import java.time.format.DateTimeFormatter;

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

    private static final Mustache QUPC_IN180000UK04_TEMPLATE =
        loadTemplate("QUPC_IN180000SM04.mustache");
    private static final String CORRELATION_ID_MDC_KEY = "CorrelationId";

    private static final String ACS_SYSTEM = "https://fhir.nhs.uk/R4/CodeSystem/SCR-ACSPermission";
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

    public String getScrIdRawXml(String nhsNumber, String nhsdAsid, String clientIp) {
        String requestBody = prepareEventListQueryRequest(nhsNumber, nhsdAsid, clientIp);
        SpineHttpClient.Response result = spineClient.sendGetScrId(requestBody, nhsdAsid);
        return result.getBody();
    }

    public Bundle getScrId(String nhsNumber, String nhsdAsid, String clientIp, String baseUrl) {
        String scrIdXml = getScrIdRawXml(nhsNumber, nhsdAsid, clientIp);

        EventListQueryResponse response = EventListQueryResponse.parseXml(scrIdXml);

        Bundle bundle = new Bundle();
        bundle.setType(SEARCHSET);
        bundle.setId(randomUUID());
        if (isNotEmpty(response.getLatestScrId()) && asList(YES, ASK).contains(response.getViewPermission())) {
            bundle.setTotal(1);

            Patient patient = buildPatientResource(nhsNumber);
            DocumentReference documentReference = buildDocumentReference(nhsNumber, baseUrl, response, patient);

            bundle.addEntry(new BundleEntryComponent()
                .setFullUrl(baseUrl + "/DocumentReference/" + documentReference.getId())
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

    private DocumentReference buildDocumentReference(String nhsNumber,
                                                     String baseUrl,
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
            buildDocumentReferenceContent(nhsNumber, baseUrl, response.getLatestScrId());
        documentReference.addContent(content);

        documentReference.setMasterIdentifier(new Identifier()
            .setValue(response.getLatestScrId())
            .setSystem(SCR_ID_SYSTEM));

        DocumentReferenceContextComponent context = new DocumentReferenceContextComponent();
        context.addEvent(GP_SUMMARY_SNOMED);
        documentReference.setContext(context);

        return documentReference;
    }

    private DocumentReferenceContentComponent buildDocumentReferenceContent(String nhsNumber,
                                                                            String baseUrl,
                                                                            String scrId) {
        DocumentReferenceContentComponent content = new DocumentReferenceContentComponent();
        String attachmentUrl = String.format(ATTACHMENT_URL, baseUrl, scrId, nhsNumber);
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
        return TemplateUtils.fillTemplate(QUPC_IN180000UK04_TEMPLATE, eventListQueryParams);
    }
}
