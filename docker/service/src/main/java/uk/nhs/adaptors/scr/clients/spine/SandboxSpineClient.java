package uk.nhs.adaptors.scr.clients.spine;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.models.ProcessingResult;

import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static uk.nhs.adaptors.scr.utils.DocumentBuilderUtil.documentBuilder;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SandboxSpineClient implements SpineClientContract {

    private static final String ACS_SET_PERMISSION_RESPONSE = "mock-spine/setConsent.xml";
    private static final String EVENT_LIST_QUERY_RESPONSE = "mock-spine/getScrId.xml";
    private static final String EVENT_QUERY_RESPONSE = "mock-spine/getScr.xml";
    private static final String UPLOAD_SCR_POLLING_RESPONSE = "mock-spine/uploadScrPolling.xml";

    private final ScrConfiguration scrConfiguration;

    @SneakyThrows
    @Override
    public Response<Document> sendAcsData(String requestBody, String nhsdAsid) {
        return new Response(OK.value(), null, getResourceAsXmlDocument(ACS_SET_PERMISSION_RESPONSE));
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
        String responseBody = getResourceAsString(UPLOAD_SCR_POLLING_RESPONSE);
        return ProcessingResult.parseProcessingResult(responseBody);
    }

    @SneakyThrows
    @Override
    public Response<Document> sendGetScrId(String requestBody, String nhsdAsid) {
        sleep(scrConfiguration.getSandboxDelay());
        return new Response(OK.value(), null, getResourceAsXmlDocument(EVENT_LIST_QUERY_RESPONSE));
    }

    @SneakyThrows
    @Override
    public Response<Document> sendGetScr(String requestBody, String nhsdAsid) {
        sleep(scrConfiguration.getSandboxDelay());
        return new Response(OK.value(), null, getResourceAsXmlDocument(EVENT_QUERY_RESPONSE));
    }

    @Override
    public Response<String> sendAlert(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        return new Response(OK.value(), null, null);
    }

    @SneakyThrows
    private static String getResourceAsString(String path) {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), UTF_8);
    }

    @SneakyThrows
    private static Document getResourceAsXmlDocument(String path) {
        return documentBuilder().parse(new ClassPathResource(path).getInputStream());
    }
}
