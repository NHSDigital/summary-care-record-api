package uk.nhs.adaptors.scr.clients;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.NoScrResultException;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;
import uk.nhs.adaptors.scr.exceptions.ScrTimeoutException;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SpineClient {
    private final SpineConfiguration spineConfiguration;
    private final SpineHttpClient spineHttpClient;

    @SneakyThrows
    public SpineHttpClient.Response sendAcsData(String requestBody) {
        var url = spineConfiguration.getUrl() + spineConfiguration.getAcsEndpoint();

        var request = new HttpPost(url);
        //TODO: set headers
        request.setEntity(new StringEntity(requestBody));

        return spineHttpClient.sendRequest(request);
    }

    @SneakyThrows
    public SpineHttpClient.Response sendScrData(String requestBody) {
        var url = spineConfiguration.getUrl() + spineConfiguration.getScrEndpoint();

        var request = new HttpPost(url);
        //TODO: set headers
        request.setEntity(new StringEntity(requestBody));

        var response = spineHttpClient.sendRequest(request);

        if (response.getStatusCode() == HttpStatus.ACCEPTED.value()) {
            return response;
        }
        throw new ScrBaseException(String.format("Unexpected response while sending SCR request: %s %s",
            response.getStatusCode(), response.getBody()));
    }

    public String getScrProcessingResult(String contentLocation, long initialWaitTime) {
        var repeatTimeout = spineConfiguration.getScrResultRepeatTimeout();
        RetryTemplate template = RetryTemplate.builder()
            .withinMillis(repeatTimeout)
            .customBackoff(new ScrRetryBackoffPolicy())
            .retryOn(NoScrResultException.class)
            .build();

        LOGGER.info("Starting polling result. First request in {}ms", initialWaitTime);
        try {
            Thread.sleep(initialWaitTime);
        } catch (InterruptedException e) {
            throw new ScrTimeoutException(e);
        }
        return template.execute(ctx -> {
            LOGGER.info("Fetching SCR processing result. RetryCount={}", ctx.getRetryCount());
            var result = fetchScrProcessingResult(contentLocation);
            int statusCode = result.getStatusCode();
            if (statusCode == HttpStatus.ACCEPTED.value()) {
                var nextRetryAfter = Long.parseLong(SpineHttpClient.getHeader(result.getHeaders(), SpineHttpClient.RETRY_AFTER_HEADER));
                LOGGER.info("{} received. NextRetry in {}ms", statusCode, nextRetryAfter);
                throw new NoScrResultException(nextRetryAfter);
            } else if (statusCode == HttpStatus.OK.value()) {
                LOGGER.info("{} received. Returning result", statusCode);
                return result.getBody();
            } else {
                LOGGER.debug("Unexpected response:\n{}\n{}", statusCode, result.getBody());
                throw new ScrBaseException("Unexpected response " + statusCode);
            }
        });
    }

    private SpineHttpClient.Response fetchScrProcessingResult(String contentLocation) {
        var request = new HttpGet(contentLocation);
        //TODO: set headers

        SpineHttpClient.Response response = spineHttpClient.sendRequest(request);
        var statusCode = response.getStatusCode();
        if (statusCode == HttpStatus.ACCEPTED.value() || statusCode == HttpStatus.OK.value()) {
            return response;
        } else {
            throw new ScrBaseException(String.format("Unexpected response while sending SCR request: %s %s",
                response.getStatusCode(), response.getBody()));
        }
    }

    public static class ScrRetryBackoffPolicy implements BackOffPolicy {
        @Override
        public BackOffContext start(RetryContext context) {
            return new ScrRetryBackOffContext(context);
        }

        @Override
        public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
            var scrRetryBackOffContext = (ScrRetryBackOffContext) backOffContext;
            var lastException = scrRetryBackOffContext.getRetryContext().getLastThrowable();
            if (lastException instanceof NoScrResultException) {
                var exception = (NoScrResultException) lastException;
                var retryAfter = exception.getRetryAfter();
                try {
                    Thread.sleep(retryAfter);
                } catch (InterruptedException e) {
                    throw new ScrTimeoutException(e);
                }
            } else {
                throw new ScrBaseException("Unexpected exception", lastException);
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class ScrRetryBackOffContext implements BackOffContext {
        private final RetryContext retryContext;
    }
}
