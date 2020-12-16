package uk.nhs.adaptors.scr.clients;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.NoSpineResultException;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;
import uk.nhs.adaptors.scr.exceptions.ScrTimeoutException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;
import uk.nhs.adaptors.scr.models.ProcessingResult;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static uk.nhs.adaptors.scr.consts.HttpHeaders.CONTENT_TYPE;
import static uk.nhs.adaptors.scr.consts.HttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.consts.HttpHeaders.SOAP_ACTION;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SpineClient implements SpineClientContract {
    private final SpineConfiguration spineConfiguration;
    private final SpineHttpClient spineHttpClient;

    @SneakyThrows
    @Override
    public SpineHttpClient.Response sendAcsData(String requestBody) {
        var url = spineConfiguration.getUrl() + spineConfiguration.getAcsEndpoint();

        var request = new HttpPost(url);
        //TODO: set headers
        request.setEntity(new StringEntity(requestBody));

        return spineHttpClient.sendRequest(request);
    }

    @SneakyThrows
    @Override
    public SpineHttpClient.Response sendScrData(String requestBody) {
        var url = spineConfiguration.getUrl() + spineConfiguration.getScrEndpoint();

        var request = new HttpPost(url);
        request.addHeader(SOAP_ACTION, "urn:nhs:names:services:psis/REPC_IN150016SM05");
        request.addHeader(CONTENT_TYPE,
            "multipart/related; boundary=\"--=_MIME-Boundary\"; type=\"text/xml\"; start=\"<ebXMLHeader@spine.nhs.uk>\"");
        request.setEntity(new StringEntity(requestBody));
        LOGGER.debug("Sending SPINE request: {}", request.getRequestLine().toString());
        var response = spineHttpClient.sendRequest(request);
        var statusCode = response.getStatusCode();

        if (statusCode != ACCEPTED.value()) {
            LOGGER.error("Unexpected spine SCR POST response: {}", response);
            throw new UnexpectedSpineResponseException("Unexpected spine 'send data' response " + statusCode);
        }
        return response;

    }

    @Override
    public ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid) {
        var repeatTimeout = spineConfiguration.getScrResultRepeatTimeout();
        var template = RetryTemplate.builder()
            .withinMillis(repeatTimeout)
            .customBackoff(new ScrRetryBackoffPolicy())
            .retryOn(NoSpineResultException.class)
            .build();

        LOGGER.info("Starting polling result. First request in {}ms", initialWaitTime);
        try {
            Thread.sleep(initialWaitTime);
        } catch (InterruptedException e) {
            throw new ScrTimeoutException(e);
        }
        return template.execute(ctx -> {
            LOGGER.info("Fetching SCR processing result. RetryCount={}", ctx.getRetryCount());

            var request = new HttpGet(spineConfiguration.getUrl() + contentLocation);
            request.addHeader("nhsd-asid", nhsdAsid);

            var result =  spineHttpClient.sendRequest(request);
            int statusCode = result.getStatusCode();

            if (statusCode == OK.value()) {
                LOGGER.info("{} processing result received.", statusCode);
                return ProcessingResult.parseProcessingResult(result.getBody());
            } else if (statusCode == ACCEPTED.value()) {
                var nextRetryAfter = Long.parseLong(SpineHttpClient.getHeader(result.getHeaders(), SpineHttpClient.RETRY_AFTER_HEADER));
                LOGGER.info("{} received. NextRetry in {}ms", statusCode, nextRetryAfter);
                throw new NoSpineResultException(nextRetryAfter);
            } else {
                LOGGER.error("Unexpected spine polling response:\n{}", result);
                throw new UnexpectedSpineResponseException("Unexpected spine polling response " + statusCode);
            }
        });
    }

    @SneakyThrows
    @Override
    public SpineHttpClient.Response sendGetScrId(String requestBody, String nhsdAsid) {
        LOGGER.debug("Sending GET SCR ID request to SPINE: {}", requestBody);
        var request = new HttpPost(spineConfiguration.getUrl() + spineConfiguration.getPsisQueriesEndpoint());
        request.addHeader(SOAP_ACTION, "urn:nhs:names:services:psisquery/QUPC_IN180000SM04");
        request.addHeader(CONTENT_TYPE, TEXT_XML_VALUE);
        request.addHeader(NHSD_ASID, nhsdAsid);

        request.setEntity(new StringEntity(requestBody));

        var response = spineHttpClient.sendRequest(request);
        var statusCode = response.getStatusCode();

        if (statusCode != OK.value()) {
            LOGGER.error("Unexpected spine GET SCR ID response: {}", response);
            throw new UnexpectedSpineResponseException("Unexpected spine send response " + statusCode);
        }
        return response;
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
            if (lastException instanceof NoSpineResultException) {
                var exception = (NoSpineResultException) lastException;
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
