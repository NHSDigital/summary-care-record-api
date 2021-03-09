package uk.nhs.adaptors.scr.clients.spine;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class SpineResponseHandler implements ResponseHandler<Response> {
    @Override
    @LogExecutionTime
    public Response handleResponse(HttpResponse response) throws IOException {
        var statusCode = response.getStatusLine().getStatusCode();
        var headers = response.getAllHeaders();
        var body = readResponseBody(response);
        LOGGER.debug("Spine response: HTTP status: {}, body: {}", statusCode, body);
        return Response.builder()
            .statusCode(statusCode)
            .headers(headers)
            .body(body)
            .build();
    }

    private static String readResponseBody(HttpResponse response) throws IOException {
        return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
    }
}
