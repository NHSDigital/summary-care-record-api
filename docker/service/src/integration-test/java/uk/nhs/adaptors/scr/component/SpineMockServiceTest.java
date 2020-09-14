package uk.nhs.adaptors.scr.component;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.scr.IntegrationTestsExtension;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.utils.spine.mock.SpineMockSetupEndpoint;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith({SpringExtension.class, IntegrationTestsExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class SpineMockServiceTest {
    private static final String HEALTHCHECK_ENDPOINT = "/healthcheck";
    private static final String ACS_ENDPOINT = "/acs";

    @Autowired
    private SpineClient spineClient;

    @Autowired
    private SpineMockSetupEndpoint spineMockSetupEndpoint;

    @Value("${spine.url}")
    private String spineUrl;

    @Test
    public void getHealthcheckShouldReturnOkStatus() {
        given()
            .baseUri(spineUrl)
            .when()
            .get(HEALTHCHECK_ENDPOINT)
            .then()
            .statusCode(OK.value()).extract();
    }

    @Test
    public void setResourceEndpointShouldPropagateResponseBackToClient() {
        String requestBody = "request";
        String responseBody = "response";

        spineMockSetupEndpoint
            .onMockServer(spineUrl)
            .forPath(ACS_ENDPOINT)
            .forHttpMethod("POST")
            .withHttpStatusCode(BAD_REQUEST.value())
            .withResponseContent(responseBody);

        var response = spineClient.sendAcsData(requestBody);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST.value());
        assertThat(response.getBody()).isEqualTo(responseBody);
    }
}
