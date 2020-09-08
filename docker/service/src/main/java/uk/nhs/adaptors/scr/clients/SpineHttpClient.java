package uk.nhs.adaptors.scr.clients;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;
import uk.nhs.adaptors.scr.services.ScrHttpClientBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SpineHttpClient {
    private final ScrHttpClientBuilder scrHttpClientBuilder;

    @SuppressFBWarnings(
        value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "SpotBugs issue with fix not yet released https://github.com/spotbugs/spotbugs/pull/1248")
    public Response sendRequest(HttpRequestBase request) {
        try (CloseableHttpClient client = scrHttpClientBuilder.build()) {
            try (CloseableHttpResponse response = client.execute(request)) {
                var statusCode = response.getStatusLine().getStatusCode();
                var headers = response.getAllHeaders();
                var body = readResponseBody(response);

                return Response.builder()
                    .statusCode(statusCode)
                    .headers(headers)
                    .body(body)
                    .build();
            }
        } catch (IOException e) {
            throw new ScrBaseException("Unexpected exception while sending ACS request", e);
        }
    }

    private String readResponseBody(CloseableHttpResponse response) throws IOException {
        return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.name());
    }

    @Builder
    @Getter
    public static class Response {
        private final int statusCode;
        private final Header[] headers;
        private final String body;
    }
}
