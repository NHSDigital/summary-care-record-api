package uk.nhs.adaptors.scr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

import static io.restassured.RestAssured.given;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.utils.spine.mock.SpineMockSetupEndpoint;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SpineMockServiceTest {
    private static final String HEALTHCHECK_ENDPOINT = "/healthcheck";
    private static final int PORT = 8081;

    @Autowired
    private SpineClient spineClient;

    @Autowired
    private SpineMockSetupEndpoint spineMockSetupEndpoint;

    @Test
    public void getHealthcheckShouldReturnOkStatus() {
        given()
            .port(PORT)
            .when()
            .get(HEALTHCHECK_ENDPOINT)
            .then()
            .statusCode(OK.value()).extract();
    }

    @Test
    public void sampleEndpointShouldReturnMockedData() {
        String message = "It's working!";

        spineMockSetupEndpoint
            .forUrl("/sample")
            .forHttpMethod("GET")
            .withHttpStatusCode(OK.value())
            .withResponseContent(message);

        String dataFromSpine = spineClient.getSampleEndpoint();

        assertThat(dataFromSpine).isEqualTo(message);
    }

    @Test
    public void setResourceEndpointShouldReturnMockedData() {
        String message = "response";

        spineMockSetupEndpoint
            .forUrl("/summary-care-record/consent")
            .forHttpMethod("POST")
            .withHttpStatusCode(OK.value())
            .withResponseContent("response");

        ResponseEntity dataFromSpine = spineClient.sendToACSEndpoint(message);

        assertThat(dataFromSpine.getStatusCode()).isEqualTo(OK);
        assertThat(dataFromSpine.getBody().toString()).isEqualTo(message);
    }
}
