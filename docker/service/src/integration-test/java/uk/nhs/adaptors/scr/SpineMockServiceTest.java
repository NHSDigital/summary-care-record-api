package uk.nhs.adaptors.scr;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.utils.spine.mock.SpineMockSetupEndpoint;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
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
    public void setResourceEndpointShouldReturn200OK() {
        String message = "response";

        spineMockSetupEndpoint
            .onMockServer(spineUrl)
            .forPath(ACS_ENDPOINT)
            .forHttpMethod("POST")
            .withHttpStatusCode(OK.value())
            .withResponseContent("response");

        ResponseEntity dataFromSpine = spineClient.sendAcsRequest(message);

        assertThat(dataFromSpine.getStatusCode()).isEqualTo(OK);
        assertThat(dataFromSpine.getBody().toString()).isEqualTo(message);
    }

    @Test
    public void setResourceEndpointShouldReturn400BadRequest() {
        String message = "response";

        spineMockSetupEndpoint
            .onMockServer(spineUrl)
            .forPath(ACS_ENDPOINT)
            .forHttpMethod("POST")
            .withHttpStatusCode(BAD_REQUEST.value())
            .withResponseContent("response");

        assertThatThrownBy(() -> spineClient.sendAcsRequest(message))
            .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    public void setResourceEndpointShouldReturn500InternalServerError() {
        String message = "response";

        spineMockSetupEndpoint
            .onMockServer(spineUrl)
            .forPath(ACS_ENDPOINT)
            .forHttpMethod("POST")
            .withHttpStatusCode(INTERNAL_SERVER_ERROR.value())
            .withResponseContent("response");

        assertThatThrownBy(() -> spineClient.sendAcsRequest(message))
            .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }
}
