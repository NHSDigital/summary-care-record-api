package uk.nhs.adaptors.scr.clients.identity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.nhs.adaptors.scr.config.IdentityServiceConfiguration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;

@RequiredArgsConstructor
@Slf4j
public class IdentityServiceClient implements IdentityServiceContract {

    private final IdentityServiceConfiguration identityServiceConfiguration;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public UserInfo getUserInfo(String baseHost, String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, authorization);

        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<UserInfo> response = restTemplate.exchange(
            baseHost + identityServiceConfiguration.getUserInfoEndpoint(), GET, entity, UserInfo.class);

        return response.getBody();
    }
}
