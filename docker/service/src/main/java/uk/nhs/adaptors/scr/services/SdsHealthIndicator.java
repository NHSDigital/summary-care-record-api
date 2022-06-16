package uk.nhs.adaptors.scr.services;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SdsHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:9001/healthcheck";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return Health.up().withDetail("Response", response).build();
            } else {
                return Health.down().withDetail("Response", response).build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("Error", "Service Unavailable").build();
        }
    }
}
