package uk.nhs.adaptors.scr.clients.spine;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.models.ProcessingResult;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.io.IOException;
import java.io.StringReader;

import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static uk.nhs.adaptors.scr.utils.DocumentBuilderUtil.documentBuilder;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SandboxSpineClient implements SpineClientContract {

    private static final String GET_SCR_ID_NHS_NUMBER_XPATH = "//nHSNumber/value/@extension";
    private static final String SET_ACS_NHS_NUMBER_XPATH =
        "//*[local-name() = 'resourceContext' and @root = '2.16.840.1.113883.2.1.4.1']/@extension";

    private static final String EXISTING_NHS_NUMBER = "9000000009";

    @Value("classpath:mock-spine/event-list-query/success.xml")
    private Resource getScrIdSuccess;

    @Value("classpath:mock-spine/event-list-query/noConsent.xml")
    private Resource getScrIdNoConsent;

    @Value("classpath:mock-spine/event-query/success.xml")
    private Resource getScrSuccess;

    @Value("classpath:mock-spine/acs/success.xml")
    private Resource setAcsSuccess;

    @Value("classpath:mock-spine/acs/incorrectNhsNumber.xml")
    private Resource setAcsIncorrectNhsNumber;

    @Value("classpath:mock-spine/upload-scr/pollingSuccess.txt")
    private Resource pollingSuccess;

    private final ScrConfiguration scrConfiguration;
    private final XmlUtils xmlUtils;

    @SneakyThrows
    @Override
    public Response<Document> sendAcsData(String requestBody, String nhsdAsid) {
        Document document = parseXml(requestBody);
        String nhsNumber = xmlUtils.getValueByXPath(document, SET_ACS_NHS_NUMBER_XPATH);
        if (EXISTING_NHS_NUMBER.equals(nhsNumber)) {
            return new Response(OK.value(), null, getResourceAsXmlDocument(setAcsSuccess));
        } else {
            return new Response(OK.value(), null, getResourceAsXmlDocument(setAcsIncorrectNhsNumber));
        }
    }

    @SneakyThrows
    @Override
    public Response<String> sendScrData(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        Header[] headers = {
            new BasicHeader(CONTENT_LOCATION, ""),
            new BasicHeader(RETRY_AFTER, "100")
        };
        sleep(scrConfiguration.getSandboxDelay());
        return new Response(ACCEPTED.value(), headers, null);
    }

    @Override
    @SneakyThrows
    public ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid,
                                                   String nhsdIdentity, String nhsdSessionUrid) {
        sleep(scrConfiguration.getSandboxDelay());
        String responseBody = getResourceAsString(pollingSuccess);
        return ProcessingResult.parseProcessingResult(responseBody);
    }

    @SneakyThrows
    @Override
    public Response<Document> sendGetScrId(String requestBody, String nhsdAsid) {
        sleep(scrConfiguration.getSandboxDelay());
        Document document = parseXml(requestBody);
        String nhsNumber = xmlUtils.getValueByXPath(document, GET_SCR_ID_NHS_NUMBER_XPATH);

        switch (nhsNumber) {
            case EXISTING_NHS_NUMBER:
                return new Response(OK.value(), null, getResourceAsXmlDocument(getScrIdSuccess));
            default:
                return new Response(OK.value(), null, getResourceAsXmlDocument(getScrIdNoConsent));
        }
    }

    @SneakyThrows
    @Override
    public Response<Document> sendGetScr(String requestBody, String nhsdAsid) {
        sleep(scrConfiguration.getSandboxDelay());
        return new Response(OK.value(), null, getResourceAsXmlDocument(getScrSuccess));
    }

    @Override
    public Response<String> sendAlert(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        return new Response(OK.value(), null, null);
    }

    @SneakyThrows
    private static String getResourceAsString(Resource resource) {
        return IOUtils.toString(resource.getInputStream(), UTF_8);
    }

    @SneakyThrows
    private static Document getResourceAsXmlDocument(Resource resource) {
        return documentBuilder().parse(resource.getInputStream());
    }

    private Document parseXml(String requestBody) throws SAXException, IOException {
        return documentBuilder().parse(new InputSource(new StringReader(requestBody)));
    }
}
