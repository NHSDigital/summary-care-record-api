package uk.nhs.adaptors.scr.component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
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
import uk.nhs.adaptors.scr.components.FhirParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static io.restassured.RestAssured.given;
import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.VALUE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class ScrTest {
    private static final String HEALTHCHECK_ENDPOINT = "/healthcheck";
    private static final String FHIR_ENDPOINT = "/Bundle";
    private static final String SCR_SPINE_ENDPOINT = "/clinical";
    private static final String SCR_SPINE_CONTENT_ENDPOINT = "/content";
    public static final String WIREMOCK_SCENARIO_NAME = "POST + polling GET";
    public static final String WIREMOCK_GET_RESPONSE_READY_STATE = "GET response ready";
    private static final int INITIAL_WAIT_TIME = 200;
    private static final int GET_WAIT_TIME = 400;
    private static final int THREAD_SLEEP_ALLOWED_DIFF = 100;
    private static final String FHIR_JSON_CONTENT_TYPE = "application/fhir+json";
    private static final String NHSD_ASID = "123";

    @LocalServerPort
    private int port;

    @Value("classpath:responses/polling/error.xml")
    private Resource pollingErrorResponse;

    @Value("classpath:responses/polling/success.xml")
    private Resource pollingSuccessResponse;

    @Value("classpath:bundle.fhir.json")
    private Resource simpleFhirJson;

    @Autowired
    private FhirParser fhirParser;

    @Value("${spine.url}")
    private String spineUrl;

    @Autowired
    private WireMockServer wireMockServer;

    private IParser parser = FhirContext.forR4().newJsonParser();

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
    }

    @Test
    public void whenGetHealthCheckThenExpect200() throws Exception {
        given()
            .port(port)
            .when()
            .get(HEALTHCHECK_ENDPOINT)
            .then()
            .statusCode(OK.value());
    }

    @Test
    public void whenPostingFhirJsonThenExpect201() throws Exception {
        whenPostingThenExpect201(
            readString(simpleFhirJson.getFile().toPath(), Charsets.UTF_8),
            FHIR_JSON_CONTENT_TYPE);
    }

    @Test
    public void whenSpinePollingReturnedErrorsThenExpect400() throws Exception {
        setUpPOSTSpineRequest();

        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT)
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(readString(pollingErrorResponse.getFile().toPath(), StandardCharsets.UTF_8))));

        warmUpWireMock();

        var body = given()
            .port(port)
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .header("Nhsd-Asid", NHSD_ASID)
            .body(readString(simpleFhirJson.getFile().toPath(), Charsets.UTF_8))
            .when()
            .post(FHIR_ENDPOINT)
            .then()
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .statusCode(BAD_REQUEST.value())
            .extract().asString();

        var operationOutcome = fhirParser.parseResource(body, OperationOutcome.class);
        assertThat(operationOutcome.getIssueFirstRep().getSeverity()).isEqualTo(ERROR);
        assertThat(operationOutcome.getIssueFirstRep().getCode()).isEqualTo(VALUE);
        assertThat(operationOutcome.getIssueFirstRep().getDetails().getText()).isEqualTo(
            "Spine processing finished with errors:\n"
                + "- 400: Invalid Request\n"
                + "- 35160: [PSIS-35160] - Invalid input message. Mandatory field NHS Number is missing or incorrect");
    }

    @Test
    public void whenSpineDoesNotReturnResultThenExpect504() throws Exception {
        setUpPOSTSpineRequest();

        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT)
                .withHeader("nhsd-asid", containing(NHSD_ASID))
                .inScenario(WIREMOCK_SCENARIO_NAME)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(GET_WAIT_TIME))));

        warmUpWireMock();

        given()
            .port(port)
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .header("Nhsd-Asid", NHSD_ASID)
            .body(readString(simpleFhirJson.getFile().toPath(), Charsets.UTF_8))
            .when()
            .post(FHIR_ENDPOINT)
            .then()
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .statusCode(GATEWAY_TIMEOUT.value());
    }

    private void whenPostingThenExpect201(String requestBody, String contentType) throws IOException {
        setUpSpineRequests();

        given()
            .port(port)
            .contentType(contentType)
            .header("Nhsd-Asid", NHSD_ASID)
            .body(requestBody)
            .when()
            .post(FHIR_ENDPOINT)
            .then()
            .statusCode(CREATED.value());

        wireMockServer.verify(1, postRequestedFor(urlEqualTo(SCR_SPINE_ENDPOINT)));
        wireMockServer.verify(2, getRequestedFor(urlEqualTo(SCR_SPINE_CONTENT_ENDPOINT)));

        List<LoggedRequest> requests = wireMockServer.findAll(RequestPatternBuilder.allRequests());

        var postRequest = requests.get(0);
        assertThat(postRequest.getAbsoluteUrl()).isEqualTo(spineUrl + SCR_SPINE_ENDPOINT);
        assertThat(postRequest.getMethod()).isEqualTo(RequestMethod.POST);
        var firstGetRequest = requests.get(1);
        assertThat(firstGetRequest.getAbsoluteUrl()).isEqualTo(spineUrl + SCR_SPINE_CONTENT_ENDPOINT);
        assertThat(firstGetRequest.getMethod()).isEqualTo(RequestMethod.GET);
        var secondGetRequest = requests.get(2);
        assertThat(secondGetRequest.getAbsoluteUrl()).isEqualTo(spineUrl + SCR_SPINE_CONTENT_ENDPOINT);
        assertThat(secondGetRequest.getMethod()).isEqualTo(RequestMethod.GET);

        var intervalBetweenPostAndFirstGet =
            (int) (firstGetRequest.getLoggedDate().getTime() - postRequest.getLoggedDate().getTime());
        assertThat(intervalBetweenPostAndFirstGet).isBetween(INITIAL_WAIT_TIME, INITIAL_WAIT_TIME + THREAD_SLEEP_ALLOWED_DIFF);

        var intervalBetweenFirstAndSecondGetGet =
            (int) (secondGetRequest.getLoggedDate().getTime() - firstGetRequest.getLoggedDate().getTime());
        assertThat(intervalBetweenFirstAndSecondGetGet).isBetween(GET_WAIT_TIME, GET_WAIT_TIME + THREAD_SLEEP_ALLOWED_DIFF);
    }

    @Test
    public void whenPostingInvalidContentThenExpect400() {
        var responseBody = given()
            .port(port)
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .header("Nhsd-Asid", NHSD_ASID)
            .body("<invalid_content>>")
            .when()
            .post(FHIR_ENDPOINT)
            .then()
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .statusCode(BAD_REQUEST.value())
            .extract()
            .asString();

        verifyOperationOutcome(responseBody, VALUE, ERROR);
    }

    @Test
    public void whenPostingMissingRequiredHeaderContentThenExpect400() throws IOException {
        var responseBody = given()
            .port(port)
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .body(readString(simpleFhirJson.getFile().toPath(), Charsets.UTF_8))
            .when()
            .post(FHIR_ENDPOINT)
            .then()
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .statusCode(BAD_REQUEST.value())
            .extract()
            .asString();

        verifyOperationOutcome(responseBody, VALUE, ERROR);
    }

    private void setUpPOSTSpineRequest() {
        wireMockServer.stubFor(
            WireMock.post(SCR_SPINE_ENDPOINT).inScenario(WIREMOCK_SCENARIO_NAME)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(INITIAL_WAIT_TIME))));
    }

    private void setUpSpineRequests() throws IOException {
        setUpPOSTSpineRequest();

        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT).inScenario(WIREMOCK_SCENARIO_NAME)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(GET_WAIT_TIME)))
                .willSetStateTo(WIREMOCK_GET_RESPONSE_READY_STATE));
        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT).inScenario(WIREMOCK_SCENARIO_NAME)
                .whenScenarioStateIs(WIREMOCK_GET_RESPONSE_READY_STATE)
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(readString(pollingSuccessResponse.getFile().toPath(), StandardCharsets.UTF_8))));

        warmUpWireMock();
    }

    private void warmUpWireMock() {
        // to warm-up wiremock so requests are returned without any delay and we could measure wait time
        given()
            .baseUri(spineUrl + SCR_SPINE_ENDPOINT)
            .post()
            .andReturn();
        given()
            .baseUri(spineUrl + SCR_SPINE_CONTENT_ENDPOINT)
            .get()
            .andReturn();

        wireMockServer.resetRequests();
        wireMockServer.resetScenarios();
    }

    private void verifyOperationOutcome(String responseBody, IssueType code, IssueSeverity severity) {
        var response = parser.parseResource(responseBody);
        assertThat(response).isInstanceOf(OperationOutcome.class);
        OperationOutcome operationOutcome = (OperationOutcome) response;
        assertThat(operationOutcome.getIssueFirstRep().getCode()).isEqualTo(code);
        assertThat(operationOutcome.getIssueFirstRep().getSeverity()).isEqualTo(severity);
    }
}
