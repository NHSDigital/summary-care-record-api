package uk.nhs.adaptors.scr.component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.scr.WireMockInitializer;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.VALUE;
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

    @LocalServerPort
    private int port;

    @Autowired
    private WireMockServer wireMockServer;

    private IParser parser = FhirContext.forR4().newJsonParser();

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
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .extract()
            .asString();

        verifyOperationOutcome(response, VALUE, ERROR, "Invalid value - INVALID in field 'patient'");
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
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .extract()
            .asString();

        verifyOperationOutcome(response, VALUE, ERROR, "Invalid value - INVALID in field 'type'");
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
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .extract()
            .asString();

        verifyOperationOutcome(response, VALUE, ERROR, "Invalid value - INVALID in field '_sort'");
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
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .extract()
            .asString();

        verifyOperationOutcome(response, VALUE, ERROR, "Invalid value - 2 in field '_count'");
    }

    @Test
    public void whenMissingRequiredParamParamThenExpectHttp400() throws IOException {
        String response = given()
            .port(port)
            .when()
            .queryParam("_sort", SORT_PARAM)
            .queryParam("_count", "2")
            .queryParam("type", TYPE_PARAM)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .header("nhsd-asid", NHSD_ASID)
            .header("client-ip", CLIENT_IP)
            .get(GET_SCR_ID_ENDPOINT)
            .then()
            .statusCode(BAD_REQUEST.value())
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .extract()
            .asString();

        verifyOperationOutcome(response, VALUE, ERROR, "Required String parameter 'patient' is not present");
    }

    private void verifyOperationOutcome(String responseBody, IssueType code, IssueSeverity severity, String details) {
        var response = parser.parseResource(responseBody);
        assertThat(response).isInstanceOf(OperationOutcome.class);
        OperationOutcome operationOutcome = (OperationOutcome) response;
        assertThat(operationOutcome.getIssueFirstRep().getCode()).isEqualTo(code);
        assertThat(operationOutcome.getIssueFirstRep().getSeverity()).isEqualTo(severity);
        assertThat(operationOutcome.getIssueFirstRep().getDetails().getText()).isEqualTo(details);
    }
}
