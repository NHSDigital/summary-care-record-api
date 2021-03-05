package uk.nhs.adaptors.scr.clients.identity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.nhs.adaptors.scr.config.IdentityServiceConfiguration;
import uk.nhs.adaptors.scr.config.ScrConfiguration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

@RequiredArgsConstructor
@Slf4j
public class IdentityServiceClient implements IdentityServiceContract {

    private final IdentityServiceConfiguration identityServiceConfiguration;
    private final ScrConfiguration scrConfiguration;
    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public UserInfo getUserInfo(String authorization) {
        LOGGER.info("Fetching UserInfo from {}", scrConfiguration.getBaseUrl() + identityServiceConfiguration.getUserInfoEndpoint());
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, authorization);

        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<UserInfo> response = restTemplate.exchange(
            scrConfiguration.getBaseUrl() + identityServiceConfiguration.getUserInfoEndpoint(), GET, entity, UserInfo.class);

        LOGGER.info("Fetched UserInfo");
        return response.getBody();
    }
}
