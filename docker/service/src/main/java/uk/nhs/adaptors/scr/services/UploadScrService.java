package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.clients.SpineClientContract;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.exceptions.ForbiddenException;
import uk.nhs.adaptors.scr.exceptions.NonSuccessSpineProcessingResultException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;
import uk.nhs.adaptors.scr.models.EventListQueryResponse;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.ProcessingResult;
import uk.nhs.adaptors.scr.models.RequestData;
import uk.nhs.adaptors.scr.utils.FhirHelper;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.parseLong;
import static java.util.Arrays.asList;
import static uk.nhs.adaptors.scr.clients.SpineHttpClient.CONTENT_LOCATION_HEADER;
import static uk.nhs.adaptors.scr.clients.SpineHttpClient.RETRY_AFTER_HEADER;
import static uk.nhs.adaptors.scr.clients.SpineHttpClient.getHeader;
import static uk.nhs.adaptors.scr.models.AcsPermission.ASK;
import static uk.nhs.adaptors.scr.models.AcsPermission.YES;

@Component
@Slf4j
public class UploadScrService {

    @Autowired
    private SpineClientContract spineClient;
    @Autowired
    private ScrConfiguration scrConfiguration;
    @Autowired
    private GetScrService getScrService;

    private static final Mustache REPC_RM150007UK05_TEMPLATE =
        TemplateUtils.loadTemplate("REPC_RM150007UK05.mustache");

    public void uploadScr(RequestData requestData) {
        var spineRequest = mapRequestData(requestData);
        checkPermission(requestData);
        var response = spineClient.sendScrData(spineRequest);

        String contentLocation;
        long retryAfter;
        try {
            contentLocation = getHeader(response.getHeaders(), CONTENT_LOCATION_HEADER);
            retryAfter = parseLong(getHeader(response.getHeaders(), RETRY_AFTER_HEADER));
        } catch (Exception ex) {
            throw new UnexpectedSpineResponseException("Unable to extract required headers", ex);
        }

        var processingResult = spineClient.getScrProcessingResult(contentLocation, retryAfter, requestData.getNhsdAsid());
        validateProcessingResult(processingResult);
    }

    private void checkPermission(RequestData requestData) {
        String nhsNumber = getNhsNumber(requestData.getBundle());
        String scrIdXml = getScrService.getScrIdRawXml(nhsNumber, requestData.getNhsdAsid(), requestData.getClientIp());
        EventListQueryResponse eventListQueryResponse = EventListQueryResponse.parseXml(scrIdXml);
        if (!asList(YES, ASK).contains(eventListQueryResponse.getStorePermission())) {
            throw new ForbiddenException("Forbidden update with error - there's no patient's consent to store SCR");
        }
    }

    private String getNhsNumber(Bundle bundle) {
        Patient patient = FhirHelper.getDomainResource(bundle, Patient.class);
        return FhirHelper.getNhsNumber(patient).getPatientId();
    }

    private String mapRequestData(RequestData requestData) {
        try {
            GpSummary gpSummary = GpSummary.fromRequestData(requestData);
            gpSummary.setPartyIdFrom(scrConfiguration.getPartyIdFrom());
            gpSummary.setPartyIdTo(scrConfiguration.getPartyIdTo());
            gpSummary.setNhsdAsidTo(scrConfiguration.getNhsdAsidTo());
            return TemplateUtils.fillTemplate(REPC_RM150007UK05_TEMPLATE, gpSummary);

        } catch (Exception ex) {
            throw new FhirMappingException(ex.getMessage());
        }
    }

    @SneakyThrows
    private void validateProcessingResult(ProcessingResult processingResult) {
        var hl7Document = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(processingResult.getHl7())));

        var acknowledgementXPath = XPathFactory.newInstance().newXPath().compile("/MCCI_IN010000UK13/acknowledgement");
        var acknowledgementNode = ((NodeList) acknowledgementXPath.evaluate(hl7Document, XPathConstants.NODESET)).item(0);
        String acknowledgementTypeCode;
        try {
            acknowledgementTypeCode = acknowledgementNode.getAttributes().getNamedItem("typeCode").getNodeValue();
        } catch (Exception ex) {
            LOGGER.error("Unable to extract acknowledgement coe:\n{}", processingResult.getHl7());
            throw new UnexpectedSpineResponseException("Unable to extract acknowledgement code");
        }

        if (!acknowledgementTypeCode.equals("AA")) {
            var errors = getErrors(hl7Document);
            LOGGER.error("Non success spine processing result:\n{}\n{}",
                processingResult.getSoapEnvelope(), processingResult.getHl7());
            throw new NonSuccessSpineProcessingResultException(errors);
        }
    }

    private List<String> getErrors(Document hl7Document) throws XPathExpressionException {
        var errorsXPath = XPathFactory.newInstance().newXPath()
            .compile("/MCCI_IN010000UK13/ControlActEvent/reason/justifyingDetectedIssueEvent/code");

        var acknowledgementNodes = (NodeList) errorsXPath.evaluate(hl7Document, XPathConstants.NODESET);

        var errorsList = new ArrayList<String>();
        for (int i = 0; i < acknowledgementNodes.getLength(); i++) {
            Node node = acknowledgementNodes.item(i);
            var code = node.getAttributes().getNamedItem("code").getTextContent();
            var displayName = node.getAttributes().getNamedItem("displayName").getTextContent();
            errorsList.add(String.format("%s: %s", code, displayName));
        }
        return errorsList;
    }
}
