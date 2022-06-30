package uk.nhs.adaptors.scr.clients.identity.sds;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
public class SdsClient {

    private final HttpClient client;
    private final PoolingHttpClientConnectionManager clientConnectionManager;

    @SuppressFBWarnings(
        value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "SpotBugs issue with fix not yet released https://github.com/spotbugs/spotbugs/pull/1248")
    @LogExecutionTime
    public <T> Response<T> sendRequest(HttpRequestBase request, ResponseHandler<? extends Response<T>> responseHandler) {
        LOGGER.debug("Attempting to send SDS request: {}", request.getRequestLine().toString());
        try {
            LOGGER.info("Leased connections: " + clientConnectionManager.getTotalStats().getLeased());
            LOGGER.info("Available connections: " + clientConnectionManager.getTotalStats().getAvailable());

            return client.execute(request, responseHandler, HttpClientContext.create());
        } catch (IOException e) {
            LOGGER.error("Error while sending SDS request", e);
            throw new ScrBaseException("Unexpected exception when sending SDS request", e);
        }
    }


//    public static String getHeader(Header[] headers, String headerName) {
//        return Arrays.stream(headers)
//            .filter(header -> header.getName().equalsIgnoreCase(headerName))
//            .map(NameValuePair::getValue)
//            .findFirst()
//            .orElseThrow(() -> new ScrBaseException("Response missing " + headerName + " header"));
//    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class Response<T> {
        private final int statusCode;
        private final Header[] headers;
        private final T body;
    }
}
