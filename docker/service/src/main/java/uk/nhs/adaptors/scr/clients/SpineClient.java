package uk.nhs.adaptors.scr.clients;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;
import uk.nhs.adaptors.scr.exceptions.ScrGatewayTimeoutException;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

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
    public String sendScrData(String requestBody) {
        var url = spineConfiguration.getUrl() + spineConfiguration.getScrEndpoint();

        var request = new HttpPost(url);
        //TODO: set headers
        request.setEntity(new StringEntity(requestBody));

        var response = spineHttpClient.sendRequest(request);

        //TODO: is 202 the expected status here?
        if (response.getStatusCode() == HttpStatus.ACCEPTED.value()) {
            return response.getBody();
        }
        throw new ScrBaseException(String.format("Unexpected response while sending SCR request: %s %s",
            response.getStatusCode(), response.getBody()));
    }

    @SneakyThrows
    public String getScrProcessingResult(String requestIdentifier) {
        var repeatTimeout = spineConfiguration.getScrResultRepeatTimeout();
        var repeatBackoff = spineConfiguration.getScrResultRepeatBackoff();
        RetryTemplate template = RetryTemplate.builder()
            .withinMillis(repeatTimeout)
            .fixedBackoff(repeatBackoff)
            .retryOn(NoScrResultException.class)
            .retryOn(ScrGatewayTimeoutException.class)
            .build();

        try {
            LOGGER.debug("Fetching SCR processing result: repeatTimeout={}ms, repeatBackoff={}ms",
                repeatTimeout, repeatBackoff);
            return template.execute(ctx -> {
                LOGGER.debug("Fetching SCR processing result: retryCount={}", ctx.getRetryCount());
                var result = fetchScrProcessingResult(requestIdentifier);
                if (result.isEmpty()) {
                    throw new NoScrResultException();
                }
                return result.get();
            });
        } catch (Exception ex) {
            if (ex instanceof NoScrResultException) {
                throw new ScrGatewayTimeoutException(
                    String.format("Repeat timeout %sms reached", spineConfiguration.getScrResultRepeatTimeout()), ex);
            }
            throw ex;
        }
    }

    private Optional<String> fetchScrProcessingResult(String requestIdentifier) {
        //TODO: check how to include the request identifier in the request
        var url = spineConfiguration.getUrl() + spineConfiguration.getScrEndpoint() + "/" + requestIdentifier;

        var request = new HttpGet(url);
        //TODO: set headers

        long hardTimeout = spineConfiguration.getScrResultHardTimeout();
        TimerTask abortRequestTask = new TimerTask() {
            @Override
            public void run() {
                request.abort();
            }
        };
        new Timer(true).schedule(abortRequestTask, hardTimeout);

        SpineHttpClient.Response response;
        try {
            response = spineHttpClient.sendRequest(request);
        } catch (Exception e) {
            throw new ScrGatewayTimeoutException(e);
        }
        var statusCode = response.getStatusCode();
        if (statusCode == HttpStatus.PROCESSING.value()) {
            return Optional.empty();
        } else if (statusCode == HttpStatus.OK.value()) {
            return Optional.of(response.getBody());
        } else {
            throw new ScrBaseException(String.format("Unexpected response while sending SCR request: %s %s",
                response.getStatusCode(), response.getBody()));
        }
    }

    public static class NoScrResultException extends Exception {
    }
}
