package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
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
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.clients.spine.SpineClientContract;
import uk.nhs.adaptors.scr.components.FhirParser;
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
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.getHeader;
import static uk.nhs.adaptors.scr.models.AcsPermission.ASK;
import static uk.nhs.adaptors.scr.models.AcsPermission.YES;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadScrService {

    private final FhirParser fhirParser;
    private final SpineClientContract spineClient;
    private final ScrConfiguration scrConfiguration;
    private final GetScrService getScrService;

    private static final Mustache REPC_RM150007UK05_TEMPLATE =
        TemplateUtils.loadTemplate("REPC_RM150007UK05.mustache");

    public void uploadScr(RequestData requestData) {
        Bundle bundle = fhirParser.parseResource(requestData.getBody(), Bundle.class);
        var spineRequest = mapRequestData(bundle, requestData.getNhsdAsid());
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
        String scrIdXml = getScrService.getScrIdRawXml(nhsNumber, nhsdAsid, clientIp);
        EventListQueryResponse eventListQueryResponse = EventListQueryResponse.parseXml(scrIdXml);
        if (!asList(YES, ASK).contains(eventListQueryResponse.getStorePermission())) {
            throw new ForbiddenException("Forbidden update with error - there's no patient's consent to store SCR");
        }
    }

    private String getNhsNumber(Bundle bundle) {
        Patient patient = FhirHelper.getDomainResource(bundle, Patient.class);
        return FhirHelper.getNhsNumber(patient);
    }

    private String mapRequestData(Bundle bundle, String nhsdAsid) {
        try {
            GpSummary gpSummary = GpSummary.fromBundle(bundle, nhsdAsid);
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
