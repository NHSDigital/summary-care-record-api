package uk.nhs.adaptors.scr.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import uk.nhs.adaptors.scr.config.SpineConfiguration;

@Component
public class SpineClient {
    private final WebClient webClient;
    private final SpineConfiguration spineConfiguration;

    @Autowired
    public SpineClient(WebClient.Builder webClientBuilder, SpineConfiguration spineConfiguration) {
        this.spineConfiguration = spineConfiguration;
        this.webClient = webClientBuilder
            .baseUrl(spineConfiguration.getUrl())
            .build();
    }

    public String getSampleEndpoint() {
        return this.webClient
            .get()
            .uri(spineConfiguration.getSampleEndpoint())
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
}
