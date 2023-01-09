package uk.nhs.adaptors.scr.clients.spine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SpineHttpClient {

    private final HttpClient client;
    private final PoolingHttpClientConnectionManager clientConnectionManager;

    @LogExecutionTime
    public <T> Response<T> sendRequest(HttpRequestBase request, ResponseHandler<? extends Response<T>> responseHandler) {
        LOGGER.debug("Attempting to send SPINE request: {}", request.getRequestLine().toString());
        try {
            LOGGER.info("Leased connections: " + clientConnectionManager.getTotalStats().getLeased());
            LOGGER.info("Available connections: " + clientConnectionManager.getTotalStats().getAvailable());

            return client.execute(request, responseHandler, HttpClientContext.create());
        } catch (IOException e) {
            LOGGER.error("Error while sending SPINE request", e);
            throw new ScrBaseException("Unexpected exception while sending Spine request", e);
        }
    }

    public static String getHeader(Header[] headers, String headerName) {
        return Arrays.stream(headers)
            .filter(header -> header.getName().equalsIgnoreCase(headerName))
            .map(NameValuePair::getValue)
            .findFirst()
            .orElseThrow(() -> new ScrBaseException("Response missing " + headerName + " header"));
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class Response<T> {
        private final int statusCode;
        private final Header[] headers;
        private final T body;
    }
}
