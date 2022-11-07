package uk.nhs.adaptors.scr.clients.identity;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.config.IdentityServiceConfiguration;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;

@RequiredArgsConstructor
@Slf4j
public class IdentityServiceClient implements IdentityServiceContract {

    private final IdentityServiceConfiguration identityServiceConfig;
    private RestTemplate restTemplate = new RestTemplate();

    @SneakyThrows
    @Override
    public UserInfo getUserInfo(String authorization) {
        LOGGER.info("Fetching UserInfo from {}", identityServiceConfig.getBaseUrl() + identityServiceConfig.getUserInfoEndpoint());
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, authorization);

        HttpEntity entity = new HttpEntity(headers);

        try {
            ResponseEntity<UserInfo> response = restTemplate.exchange(
                identityServiceConfig.getBaseUrl() + identityServiceConfig.getUserInfoEndpoint(), GET, entity, UserInfo.class);

            LOGGER.debug("Fetched UserInfo: " + new ObjectMapper().writeValueAsString(response.getBody()));
            return response.getBody();
        } catch (HttpClientErrorException.BadRequest e) {
            LOGGER.debug("Unable to find user info for", authorization);
            throw new BadRequestException(e.getMessage());
        }
    }
}
