package uk.nhs.adaptors.scr.clients;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import uk.nhs.adaptors.scr.clients.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.models.ProcessingResult;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.OK;

public class MockSpineClient implements SpineClient {

    private static final String EVENT_LIST_QUERY_RESPONSE = "mock-spine/QUPC_IN200000SM04/success.xml";

    @Override
    public Response sendAcsData(String requestBody) {
        return null;
    }

    @Override
    public Response sendScrData(String requestBody) {
        return null;
    }

    @Override
    public ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid) {
        return null;
    }

    @SneakyThrows
    @Override
    public Response sendGetScrId(String requestBody, String nhsdAsid) {
        String responseBody = getResourceAsString(EVENT_LIST_QUERY_RESPONSE);
        return new Response(OK.value(), null, responseBody);
    }

    private static String getResourceAsString(String path) {
        try {
            return IOUtils.toString(new ClassPathResource(path).getInputStream(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
