package uk.nhs.adaptors.scr.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.scr.WireMockInitializer;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class GetScrControllerTest {

    private static final String GET_SCR_ID_ENDPOINT = "/DocumentReference";
    private static final String NHS_NUMBER = "https://fhir.nhs.uk/Id/nhs-number|9995000180";
    private static final String SORT_PARAM = "date";
    private static final String COUNT_PARAM = "1";
    private static final String TYPE_PARAM = "http://snomed.info/sct|196981000000101";
    private static final String NHSD_ASID = "1029384756";
    private static final String CLIENT_IP = "192.168.0.24";
    private static final String CLIENT_REQUEST_URL = "localhost:9000/DocumentReference";

    @LocalServerPort
    private int port;

    @Value("classpath:error-handling/OperationOutcome.template.json")
    private Resource operationOutcomeTemplate;

    @Autowired
    private WireMockServer wireMockServer;

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
    }

    @Test
    public void whenInvalidNhsNumberThenExpectHttp400() throws IOException {
        String response = given()
            .port(port)
            .when()
            .queryParam("patient", "INVALID")
            .queryParam("_sort", SORT_PARAM)
            .queryParam("_count", COUNT_PARAM)
            .queryParam("type", TYPE_PARAM)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .header("nhsd-asid", NHSD_ASID)
            .header("client-ip", CLIENT_IP)
            .header("client-request-url", CLIENT_REQUEST_URL)
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .extract()
            .asString();

        assertThat(response).isEqualTo(
            getOperationOutcome("error", "value", "Invalid value - INVALID in field 'patient'"));
    }

    @Test
    public void whenInvalidTypeParamThenExpectHttp400() throws IOException {
        String response = given()
            .port(port)
            .when()
            .queryParam("patient", NHS_NUMBER)
            .queryParam("_sort", SORT_PARAM)
            .queryParam("_count", COUNT_PARAM)
            .queryParam("type", "INVALID")
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .header("nhsd-asid", NHSD_ASID)
            .header("client-ip", CLIENT_IP)
            .header("client-request-url", CLIENT_REQUEST_URL)
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .extract()
            .asString();

        assertThat(response).isEqualTo(
            getOperationOutcome("error", "value", "Invalid value - INVALID in field 'type'"));
    }

    @Test
    public void whenInvalidSortParamThenExpectHttp400() throws IOException {
        String response = given()
            .port(port)
            .when()
            .queryParam("patient", NHS_NUMBER)
            .queryParam("_sort", "INVALID")
            .queryParam("_count", COUNT_PARAM)
            .queryParam("type", TYPE_PARAM)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .header("nhsd-asid", NHSD_ASID)
            .header("client-ip", CLIENT_IP)
            .header("client-request-url", CLIENT_REQUEST_URL)
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .extract()
            .asString();

        assertThat(response).isEqualTo(
            getOperationOutcome("error", "value", "Invalid value - INVALID in field '_sort'"));
    }

    @Test
    public void whenInvalidCountParamThenExpectHttp400() throws IOException {
        String response = given()
            .port(port)
            .when()
            .queryParam("patient", NHS_NUMBER)
            .queryParam("_sort", SORT_PARAM)
            .queryParam("_count", "2")
            .queryParam("type", TYPE_PARAM)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .header("nhsd-asid", NHSD_ASID)
            .header("client-ip", CLIENT_IP)
            .header("client-request-url", CLIENT_REQUEST_URL)
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .extract()
            .asString();

        assertThat(response).isEqualTo(
            getOperationOutcome("error", "value", "Invalid value - 2 in field '_count'"));
    }

    private String getOperationOutcome(String severity, String code, String message) throws IOException {
        String operationOutcome = readString(operationOutcomeTemplate.getFile().toPath(), UTF_8).trim();
        return String.format(operationOutcome, severity, code, message);
    }
}
