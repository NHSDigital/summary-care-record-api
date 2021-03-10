package uk.nhs.adaptors.scr.clients.spine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.NoSpineResultException;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;
import uk.nhs.adaptors.scr.exceptions.ScrTimeoutException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;
import uk.nhs.adaptors.scr.models.ProcessingResult;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static uk.nhs.adaptors.scr.config.ConversationIdFilter.CORRELATION_ID_MDC_KEY;
import static uk.nhs.adaptors.scr.config.RequestIdFilter.REQUEST_ID_MDC_KEY;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_CORRELATION_ID;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_IDENTITY;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_REQUEST_ID;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_SESSION_URID;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.SOAP_ACTION;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SpineClient implements SpineClientContract {

    private static final String UPLOAD_SCR_SOAP_ACTION = "urn:nhs:names:services:psis/REPC_IN150016SM05";
    private static final String UPLOAD_SCR_CONTENT_TYPE =
        "multipart/related; boundary=\"--=_MIME-Boundary\"; type=\"text/xml\"; start=\"<ebXMLHeader@spine.nhs.uk>\"";
    private static final String GET_SCR_ID_SOAP_ACTION = "urn:nhs:names:services:psisquery/QUPC_IN180000SM04";
    private static final String SET_PERMISSION_SOAP_ACTION = "urn:nhs:names:services:lrs/SET_RESOURCE_PERMISSIONS_INUK01";

    private final SpineConfiguration spineConfiguration;
    private final SpineHttpClient spineHttpClient;
    private final SpineStringResponseHandler stringResponseHandler;
    private final SpineXmlResponseHandler xmlResponseHandler;

    @SneakyThrows
    @Override
    @LogExecutionTime
    public Response<Document> sendAcsData(String requestBody, String nhsdAsid) {
        var url = spineConfiguration.getUrl() + spineConfiguration.getAcsEndpoint();
        LOGGER.info("Sending ACS Set Permission Spine request. URL: {}", url);
        LOGGER.debug("Body: {}", requestBody);
        var request = new HttpPost(url);
        setSoapHeaders(request, SET_PERMISSION_SOAP_ACTION, TEXT_XML_VALUE);
        request.setHeader(NHSD_ASID, nhsdAsid);
        request.setEntity(new StringEntity(requestBody));

        var response = spineHttpClient.sendRequest(request, xmlResponseHandler);
        var statusCode = response.getStatusCode();

        if (statusCode != OK.value()) {
            LOGGER.error("Unexpected spine ACS set permission response: {}", response);
            throw new UnexpectedSpineResponseException("Unexpected spine 'send data' response " + statusCode);
        }
        return response;
    }

    @SneakyThrows
    @Override
    @LogExecutionTime
    public Response<String> sendScrData(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        var url = spineConfiguration.getUrl() + spineConfiguration.getScrEndpoint();
        LOGGER.info("Sending SCR Upload request to SPINE. URL: {}", url);
        LOGGER.debug("Body: {}", requestBody);

        var request = new HttpPost(url);
        setUploadScrHeaders(request, nhsdAsid, nhsdIdentity, nhsdSessionUrid);
        request.setEntity(new StringEntity(requestBody));

        var response = spineHttpClient.sendRequest(request, stringResponseHandler);
        var statusCode = response.getStatusCode();

        if (statusCode != ACCEPTED.value()) {
            LOGGER.error("Unexpected spine SCR POST response: {}", response);
            throw new UnexpectedSpineResponseException("Unexpected spine 'send data' response " + statusCode);
        }
        return response;

    }

    private void setUploadScrHeaders(HttpRequest request, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        setSoapHeaders(request, UPLOAD_SCR_SOAP_ACTION, UPLOAD_SCR_CONTENT_TYPE);
        setCommonHeaders(request, nhsdAsid, nhsdIdentity, nhsdSessionUrid);
    }

    private void setCommonHeaders(HttpRequest request, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        request.setHeader(NHSD_ASID, nhsdAsid);
        request.setHeader(NHSD_IDENTITY, nhsdIdentity);
        request.setHeader(NHSD_SESSION_URID, nhsdSessionUrid);
        request.setHeader(NHSD_CORRELATION_ID, MDC.get(CORRELATION_ID_MDC_KEY));
        request.setHeader(NHSD_REQUEST_ID, MDC.get(REQUEST_ID_MDC_KEY));
    }

    private void setSoapHeaders(HttpRequest request, String soapAction, String contentType) {
        request.addHeader(SOAP_ACTION, soapAction);
        request.addHeader(CONTENT_TYPE, contentType);
    }

    @Override
    @LogExecutionTime
    public ProcessingResult getScrProcessingResult(String contentLocation, long initialWaitTime, String nhsdAsid,
                                                   String nhsdIdentity, String nhsdSessionUrid) {
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
            setCommonHeaders(request, nhsdAsid, nhsdIdentity, nhsdSessionUrid);

            var result = spineHttpClient.sendRequest(request, stringResponseHandler);
            int statusCode = result.getStatusCode();

            if (statusCode == OK.value()) {
                LOGGER.info("{} processing result received.", statusCode);
                return ProcessingResult.parseProcessingResult(result.getBody());
            } else if (statusCode == ACCEPTED.value()) {
                var nextRetryAfter = Long.parseLong(SpineHttpClient.getHeader(result.getHeaders(), RETRY_AFTER));
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
    @LogExecutionTime
    public Response<Document> sendGetScrId(String requestBody, String nhsdAsid) {
        LOGGER.info("Sending GET SCR ID Spine request");
        LOGGER.debug("Body: {}", requestBody);
        var request = new HttpPost(spineConfiguration.getUrl() + spineConfiguration.getPsisQueriesEndpoint());
        setSoapHeaders(request, GET_SCR_ID_SOAP_ACTION, TEXT_XML_VALUE);

        request.setEntity(new StringEntity(requestBody));

        var response = spineHttpClient.sendRequest(request, xmlResponseHandler);
        var statusCode = response.getStatusCode();

        if (statusCode != OK.value()) {
            LOGGER.error("Unexpected spine GET SCR ID response: {}", response);
            throw new UnexpectedSpineResponseException("Unexpected spine send response " + statusCode);
        }
        return response;
    }

    @Override
    @SneakyThrows
    @LogExecutionTime
    public Response<String> sendAlert(String requestBody, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        LOGGER.info("Sending ALERT Spine request");
        LOGGER.debug("Body: {}", requestBody);
        var request = new HttpPost(spineConfiguration.getUrl() + spineConfiguration.getAlertEndpoint());
        request.addHeader(CONTENT_TYPE, APPLICATION_FHIR_JSON_VALUE);
        setCommonHeaders(request, nhsdAsid, nhsdIdentity, nhsdSessionUrid);
        request.setEntity(new StringEntity(requestBody));

        return spineHttpClient.sendRequest(request, stringResponseHandler);
    }

    @SneakyThrows
    @Override
    @LogExecutionTime
    public Response<Document> sendGetScr(String requestBody, String nhsdAsid) {
        var uri = spineConfiguration.getUrl() + spineConfiguration.getPsisQueriesEndpoint();
        var request = new HttpPost(uri);
        LOGGER.info("Sending GET SCR Spine request. URL: {}", uri);
        LOGGER.debug("Body: {}", requestBody);
        request.addHeader(SOAP_ACTION, "urn:nhs:names:services:psisquery/QUPC_IN190000UK04");
        request.addHeader(CONTENT_TYPE, TEXT_XML_VALUE);
        request.addHeader(NHSD_ASID, nhsdAsid);

        request.setEntity(new StringEntity(requestBody));

        var response = spineHttpClient.sendRequest(request, xmlResponseHandler);
        var statusCode = response.getStatusCode();

        if (statusCode != OK.value()) {
            LOGGER.error("Unexpected spine GET SCR response: {}", response);
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
