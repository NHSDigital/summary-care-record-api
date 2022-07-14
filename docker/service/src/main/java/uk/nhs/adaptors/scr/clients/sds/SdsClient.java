package uk.nhs.adaptors.scr.clients.sds;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSdsResponseException;

import java.net.URI;

public class SdsClient {
    public Bundle sendGet(URI uri) {

        WebClient client = WebClient.create();
        WebClient.ResponseSpec responseSpec = client.get()
            .uri(uri)
            .retrieve()
            .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
                response -> response.bodyToMono(String.class).map(UnexpectedSdsResponseException::new))
            .onStatus(HttpStatus.BAD_REQUEST::equals,
                response -> response.bodyToMono(String.class).map(BadRequestException::new));

        return responseSpec.bodyToMono(Bundle.class).block();
    }
}
