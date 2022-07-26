package uk.nhs.adaptors.scr.clients.sds;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSdsResponseException;

import java.net.URI;

import static uk.nhs.adaptors.scr.config.ConversationIdFilter.CORRELATION_ID_MDC_KEY;
import static uk.nhs.adaptors.scr.config.RequestIdFilter.REQUEST_ID_MDC_KEY;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_IDENTITY;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_SESSION_URID;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_CORRELATION_ID;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.NHSD_REQUEST_ID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SdsClient {
    private FhirParser fhirParser = new FhirParser();

    public Bundle sendGet(URI uri, String nhsdAsid, String nhsdIdentity, String nhsdSessionUrid) {
        WebClient client = WebClient.builder().defaultHeaders(httpHeaders -> {
            httpHeaders.add(NHSD_ASID, nhsdAsid);
            httpHeaders.add(NHSD_IDENTITY, nhsdIdentity);
            httpHeaders.add(NHSD_SESSION_URID, nhsdSessionUrid);
            httpHeaders.add(NHSD_CORRELATION_ID, MDC.get(CORRELATION_ID_MDC_KEY));
            httpHeaders.add(NHSD_REQUEST_ID, MDC.get(REQUEST_ID_MDC_KEY));
        }).build();

        LOGGER.info("Sending request to SDS");

        WebClient.ResponseSpec responseSpec = client.get()
            .uri(uri)
            .retrieve()
            .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
                response -> response.bodyToMono(String.class).map(UnexpectedSdsResponseException::new))
            .onStatus(HttpStatus.BAD_REQUEST::equals,
                response -> response.bodyToMono(String.class).map(BadRequestException::new));

        var strResponse = responseSpec.bodyToMono(String.class).block();

        // todo remove this
        LOGGER.info(String.format("SDS response: %s", strResponse));

        return fhirParser.parseResource(strResponse, Bundle.class);
    }
}
