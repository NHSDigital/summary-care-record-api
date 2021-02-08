package uk.nhs.adaptors.scr.clients.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SandboxIdentityServiceClient implements IdentityServiceContract {

    public static final String GET_USER_INFO_RESPONSE = "mock-identity-service/userInfo.json";

    @Override
    @SneakyThrows
    public UserInfo getUserInfo(String authorization) {
        String responseBody = IOUtils.toString(new ClassPathResource(GET_USER_INFO_RESPONSE).getInputStream(), UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseBody, UserInfo.class);
    }
}
