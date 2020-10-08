package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
import lombok.extern.slf4j.Slf4j;
import static uk.nhs.adaptors.scr.utils.ScrResponseParser.parseQueryListResponseXml;
import static uk.nhs.adaptors.scr.utils.ScrResponseParser.parseQueryResponseXml;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.DocumentException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.github.mustachejava.Mustache;

import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.clients.SpineHttpClient;
import uk.nhs.adaptors.scr.exceptions.ScrNoConsentException;
import uk.nhs.adaptors.scr.hl7tofhirmappers.DocumentReferenceMapper;
import uk.nhs.adaptors.scr.hl7tofhirmappers.PatientMapper;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.hl7models.DocumentReferenceObject;
import uk.nhs.adaptors.scr.utils.GpSummaryParser;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

@Component
@Slf4j
public class ScrService {

    private static final PatientMapper patientMapper = new PatientMapper();
    private static final DocumentReferenceMapper documentReferenceMapper = new DocumentReferenceMapper(patientMapper);
    private static final Mustache REPC_RM150007UK05_TEMPLATE = TemplateUtils.loadTemplate("REPC_RM150007UK05.mustache");
    private static final Mustache ACS_GET_RESOURCE_PERMISSIONS_TEMPLATE = TemplateUtils.loadTemplate("acs_get_resource_permissions" +
        ".mustache");
    private static final Mustache ACS_GET_SCR_TEMPLATE = TemplateUtils.loadTemplate("acs_summary_care_record.mustache");
    @Autowired
    private SpineClient spineClient;

    private static final Mustache REPC_RM150007UK05_TEMPLATE =
        TemplateUtils.loadTemplate("REPC_RM150007UK05.mustache");

    public void handleFhir(Bundle resource) {
        GpSummary gpSummary = GpSummaryParser.parseFromBundle(resource);
        String spineRequest = TemplateUtils.fillTemplate(REPC_RM150007UK05_TEMPLATE, gpSummary);

        var response = spineClient.sendScrData(spineRequest);

        var contentLocation = SpineHttpClient.getHeader(response.getHeaders(), SpineHttpClient.CONTENT_LOCATION_HEADER);
        var retryAfter = Long.parseLong(SpineHttpClient.getHeader(response.getHeaders(), SpineHttpClient.RETRY_AFTER_HEADER));

        spineClient.getScrProcessingResult(contentLocation, retryAfter);
        //TODO: should we map response to FHIR and return back to controller?
    }

    public DocumentReference getSummaryCareRecord(int patientId, String patientUUID) throws DocumentException {
        Map<String, Object> context = new HashMap<>();
        context.put("patientId", patientId);

        String acsGetResourcePermissionsRequest = TemplateUtils.fillTemplate(ACS_GET_RESOURCE_PERMISSIONS_TEMPLATE, context);

        String spinePatientUUID = getQueryListResponse(acsGetResourcePermissionsRequest);

        if (patientUUID == null) {
            patientUUID = "";
        }

        return getQueryResponse(patientUUID, spinePatientUUID, context);
    }

    private String getQueryListResponse(String acsGetResourcePermissionsRequest) throws DocumentException {
        try {
            String scrResponse = spineClient
                .sendSpineRequest(acsGetResourcePermissionsRequest)
                .getBody();
            return parseQueryListResponseXml(scrResponse);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ScrNoConsentException("no consent" + e.getMessage());
        } catch (ResourceAccessException e) {
            throw new ScrNoConsentException("no consent");
        }
    }

    private DocumentReference getQueryResponse(String patientUUID, String spinePatientUUID, Map<String, Object> context) throws DocumentException {
        if (!patientUUID.equals(spinePatientUUID)) {
            context.put("patientUUID", patientUUID);
            String scrRequest = TemplateUtils.fillTemplate(ACS_GET_SCR_TEMPLATE, context);
            String scrResponse = spineClient
                .sendSpineRequest(scrRequest)
                .getBody();
            DocumentReferenceObject documentReferenceObject = parseQueryResponseXml(scrResponse);

            return documentReferenceMapper.mapDocumentReference(documentReferenceObject);
        } else {
            return documentReferenceMapper.mapDocumentReferenceMinimal();
        }
    }
}
