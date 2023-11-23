package uk.nhs.adaptors.scr.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.clients.spine.SpineClientContract;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.NonSuccessSpineProcessingResultException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;
import uk.nhs.adaptors.scr.mappings.from.fhir.BundleMapper;
import uk.nhs.adaptors.scr.models.ProcessingResult;
import uk.nhs.adaptors.scr.models.RequestData;
import uk.nhs.adaptors.scr.utils.FhirHelper;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.parseLong;
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.getHeader;
import static uk.nhs.adaptors.scr.utils.DocumentBuilderUtil.parseDocument;

/**
 * Service to upload HL7 based SCRs to Spine.
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadScrService {

    private final FhirParser fhirParser;
    private final SpineClientContract spineClient;
    private final GetScrService getScrService;
    private final BundleMapper bundleMapper;
    private final SpineResponseParser spineResponseParser;
    private final SpineDetectedIssuesHandler spineDetectedIssuesHandler;

    @LogExecutionTime
    public void uploadScr(RequestData requestData) {
        Bundle bundle = fhirParser.parseResource(requestData.getBody(), Bundle.class);
        var spineRequest = bundleMapper.map(bundle, requestData.getNhsdAsid());
        checkPermission(bundle, requestData.getNhsdAsid(), requestData.getClientIp());
        var response = spineClient.sendScrData(spineRequest, requestData.getNhsdAsid(),
            requestData.getNhsdIdentity(), requestData.getNhsdSessionUrid());

        String contentLocation;
        long retryAfter;
        try {
            contentLocation = getHeader(response.getHeaders(), CONTENT_LOCATION);
            retryAfter = parseLong(getHeader(response.getHeaders(), RETRY_AFTER));
        } catch (Exception ex) {
            throw new UnexpectedSpineResponseException("Unable to extract required headers", ex);
        }

        var processingResult = spineClient.getScrProcessingResult(contentLocation, retryAfter, requestData.getNhsdAsid(),
            requestData.getNhsdIdentity(), requestData.getNhsdSessionUrid());
        validateProcessingResult(processingResult);
    }

    private void checkPermission(Bundle bundle, String nhsdAsid, String clientIp) {
        LOGGER.info("Checking permission to store SCR");
        String nhsNumber = getNhsNumber(bundle);
        Document scrIdXml = getScrService.getScrIdRawXml(nhsNumber, nhsdAsid, clientIp);
        var detectedIssues = spineResponseParser.getDetectedIssues(scrIdXml);
        spineDetectedIssuesHandler.handleDetectedIssues(detectedIssues);
    }

    private String getNhsNumber(Bundle bundle) {
        Patient patient = FhirHelper.getDomainResource(bundle, Patient.class);
        return FhirHelper.getNhsNumber(patient);
    }

    @SneakyThrows
    private void validateProcessingResult(ProcessingResult processingResult) {
        var hl7Document = parseDocument(processingResult.getHl7());

        var acknowledgementXPath = XPathFactory.newInstance().newXPath().compile("/MCCI_IN010000UK13/acknowledgement");
        var acknowledgementNode = ((NodeList) acknowledgementXPath.evaluate(hl7Document, XPathConstants.NODESET)).item(0);
        String acknowledgementTypeCode;
        try {
            acknowledgementTypeCode = acknowledgementNode.getAttributes().getNamedItem("typeCode").getNodeValue();
        } catch (Exception ex) {
            LOGGER.error("Unable to extract acknowledgement code:\n{}", processingResult.getHl7());
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
