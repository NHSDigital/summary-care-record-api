package uk.nhs.adaptors.scr.clients.spine;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.core.io.ClassPathResource;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.models.ProcessingResult;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.CONTENT_LOCATION_HEADER;
import static uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.RETRY_AFTER_HEADER;

public class SandboxSpineClient implements SpineClientContract {

    private static final String ACS_SET_PERMISSION_RESPONSE = "mock-spine/setConsent.xml";
    private static final String EVENT_LIST_QUERY_RESPONSE = "mock-spine/getScrId.xml";
    private static final String UPLOAD_SCR_POLLING_RESPONSE = "mock-spine/uploadScrPolling.xml";

    @Override
    public Response sendAcsData(String requestBody, String nhsdAsid) {
        String responseBody = getResourceAsString(ACS_SET_PERMISSION_RESPONSE);
        return new Response(OK.value(), null, responseBody);
    }

    @Override
    public Response sendScrData(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        Header[] headers = {
            new BasicHeader(CONTENT_LOCATION_HEADER, ""),
            new BasicHeader(RETRY_AFTER_HEADER, "300")
        };
        return new Response(ACCEPTED.value(), headers, null);
    }

    @Override
    public ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid,
                                                   String nhsdIdentity, String nhsdSessionUrid) {
        String responseBody = getResourceAsString(UPLOAD_SCR_POLLING_RESPONSE);
        return ProcessingResult.parseProcessingResult(responseBody);
    }

    @Override
    public Response sendGetScrId(String requestBody, String nhsdAsid) {
        String responseBody = getResourceAsString(EVENT_LIST_QUERY_RESPONSE);
        return new Response(OK.value(), null, responseBody);
    }

    @Override
    public Response sendAlert(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        return new Response(OK.value(), null, null);
    }

    @SneakyThrows
    private static String getResourceAsString(String path) {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), UTF_8);
    }
}
