package uk.nhs.adaptors.scr.clients.spine;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SpineHttpClient {

    private final CloseableHttpClient client;

    @SuppressFBWarnings(
        value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "SpotBugs issue with fix not yet released https://github.com/spotbugs/spotbugs/pull/1248")
    @LogExecutionTime
    public Response sendRequest(HttpRequestBase request) {
        LOGGER.debug("Attempting to send SPINE request: {}", request.getRequestLine().toString());
        try {
            try (CloseableHttpResponse response = client.execute(request)) {
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
        } catch (IOException e) {
            LOGGER.error("Error while sending SPINE request", e);
            throw new ScrBaseException("Unexpected exception while sending Spine request", e);
        }
    }

    public static String getHeader(Header[] headers, String headerName) {
        return Arrays.stream(headers)
            .filter(header -> header.getName().equals(headerName))
            .map(NameValuePair::getValue)
            .findFirst()
            .orElseThrow(() -> new ScrBaseException("Response missing " + headerName + " header"));
    }

    private String readResponseBody(CloseableHttpResponse response) throws IOException {
        return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
    }

    @Builder
    @Getter
    @ToString
    @AllArgsConstructor
    public static class Response {
        private final int statusCode;
        private final Header[] headers;
        private final String body;
    }
}
