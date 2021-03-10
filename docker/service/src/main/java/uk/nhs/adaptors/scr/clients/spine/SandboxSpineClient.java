package uk.nhs.adaptors.scr.clients.spine;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.models.ProcessingResult;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static uk.nhs.adaptors.scr.utils.XmlUtils.documentBuilder;

public class SandboxSpineClient implements SpineClientContract {

    private static final String ACS_SET_PERMISSION_RESPONSE = "mock-spine/setConsent.xml";
    private static final String EVENT_LIST_QUERY_RESPONSE = "mock-spine/getScrId.xml";
    private static final String EVENT_QUERY_RESPONSE = "mock-spine/getScr.xml";
    private static final String UPLOAD_SCR_POLLING_RESPONSE = "mock-spine/uploadScrPolling.xml";

    @SneakyThrows
    @Override
    public Response<Document> sendAcsData(String requestBody, String nhsdAsid) {
        String responseBody = getResourceAsString(ACS_SET_PERMISSION_RESPONSE);
        return new Response(OK.value(), null, documentBuilder().parse(toInputStream(responseBody, "UTF_8")));
    }

    @Override
    public Response<String> sendScrData(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        Header[] headers = {
            new BasicHeader(CONTENT_LOCATION, ""),
            new BasicHeader(RETRY_AFTER, "300")
        };
        return new Response(ACCEPTED.value(), headers, null);
    }

    @Override
    public ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid,
                                                   String nhsdIdentity, String nhsdSessionUrid) {
        String responseBody = getResourceAsString(UPLOAD_SCR_POLLING_RESPONSE);
        return ProcessingResult.parseProcessingResult(responseBody);
    }

    @SneakyThrows
    @Override
    public Response<Document> sendGetScrId(String requestBody, String nhsdAsid) {
        String responseBody = getResourceAsString(EVENT_LIST_QUERY_RESPONSE);
        return new Response(OK.value(), null, documentBuilder().parse(toInputStream(responseBody, UTF_8)));
    }

    @SneakyThrows
    @Override
    public Response<Document> sendGetScr(String requestBody, String nhsdAsid) {
        String responseBody = getResourceAsString(EVENT_QUERY_RESPONSE);
        return new Response(OK.value(), null, documentBuilder().parse(toInputStream(responseBody, UTF_8)));
    }

    @Override
    public Response<String> sendAlert(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        return new Response(OK.value(), null, null);
    }

    @SneakyThrows
    private static String getResourceAsString(String path) {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), UTF_8);
    }
}
