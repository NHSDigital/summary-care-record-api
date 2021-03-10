package uk.nhs.adaptors.scr.clients.spine;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@Slf4j
public class SpineStringResponseHandler implements ResponseHandler<Response<String>> {
    @SneakyThrows
    @Override
    @LogExecutionTime
    public Response<String> handleResponse(HttpResponse response) {
        var statusCode = response.getStatusLine().getStatusCode();
        var headers = response.getAllHeaders();
        String responseBody = readResponseBody(response);
        LOGGER.debug("Spine String response: HTTP status: {}, body: {}", statusCode, responseBody);
        return new Response<>(statusCode, headers, responseBody);
    }

    private static String readResponseBody(HttpResponse response) throws IOException {
        return IOUtils.toString(response.getEntity().getContent(), UTF_8.name());
    }
}
