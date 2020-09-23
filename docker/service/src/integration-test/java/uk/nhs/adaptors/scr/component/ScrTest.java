package uk.nhs.adaptors.scr.component;

import ca.uhn.fhir.context.FhirContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;
import uk.nhs.adaptors.scr.WireMockInitializer;
import uk.nhs.adaptors.scr.components.FhirParser;

import java.nio.file.Files;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class ScrTest {
    private static final String HEALTHCHECK_ENDPOINT = "/healthcheck";
    private static final String FHIR_ENDPOINT = "/fhir";
    private static final String SCR_SPINE_ENDPOINT = "/summarycarerecord";
    private static final String SCR_SPINE_CONTENT_ENDPOINT = "/content";
    private static final String RESPONSE_BODY = "response-body";
    public static final String WIREMOCK_SCENARIO_NAME = "POST + polling GET";
    public static final String WIREMOCK_GET_RESPONSE_READY_STATE = "GET response ready";
    private static final int INITIAL_WAIT_TIME = 200;
    private static final int GET_WAIT_TIME = 400;
    private static final int THREAD_SLEEP_ALLOWED_DIFF = 100;

    @Autowired
    private MockMvc mockMvc;

    @Value("classpath:bundle.fhir.json")
    private Resource simpleFhirJson;

    @Value("classpath:bundle.fhir.xml")
    private Resource simpleFhirXml;

    @Autowired
    private FhirParser fhirParser;

    @Value("${spine.url}")
    private String spineUrl;

    @Autowired
    private WireMockServer wireMockServer;

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
    }

    @Test
    public void whenGetHealthCheckThenExpect200() throws Exception {
        mockMvc.perform(get(HEALTHCHECK_ENDPOINT))
            .andExpect(status().isOk());
    }

    @Test
    public void whenPostingFhirJsonThenExpect200() throws Exception {
        whenPostingThenExpect200(
            Files.readString(simpleFhirJson.getFile().toPath(), Charsets.UTF_8),
            "application/fhir+json");
    }

    @Test
    public void whenPostingFhirXmlThenExpect200() throws Exception {
        whenPostingThenExpect200(
            Files.readString(simpleFhirXml.getFile().toPath(), Charsets.UTF_8),
            "application/fhir+xml");
    }

    @Test
    public void whenUnableToParseJsonDataThenExpect400() throws Exception {
        MvcResult result = mockMvc
            .perform(post(FHIR_ENDPOINT)
                .contentType("application/fhir+json")
                .content("qwe"))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isBadRequest());

        FhirContext ctx = FhirContext.forR4();
        String responseBody = result.getResponse().getContentAsString();
        var response = ctx.newJsonParser().parseResource(responseBody);

        assertThat(response).isInstanceOf(OperationOutcome.class);
    }

    @Test
    public void whenUnableToParseXmlDataThenExpect400() throws Exception {
        MvcResult result = mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+xml")
                .content("qwe"))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isBadRequest());

        FhirContext ctx = FhirContext.forR4();
        String responseBody = result.getResponse().getContentAsString();
        var response = ctx.newXmlParser().parseResource(responseBody);

        assertThat(response).isInstanceOf(OperationOutcome.class);
    }

    private void whenPostingThenExpect200(String requestBody, String contentType) throws Exception {
        setUpSpineRequests();

        var result = mockMvc
            .perform(post(FHIR_ENDPOINT)
                .contentType(contentType)
                .content(requestBody))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());

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

    private void setUpSpineRequests() {
        wireMockServer.stubFor(
            WireMock.post(SCR_SPINE_ENDPOINT).inScenario(WIREMOCK_SCENARIO_NAME)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", spineUrl + SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(INITIAL_WAIT_TIME))));
        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT).inScenario(WIREMOCK_SCENARIO_NAME)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", spineUrl + SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(GET_WAIT_TIME)))
                .willSetStateTo(WIREMOCK_GET_RESPONSE_READY_STATE));
        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT).inScenario(WIREMOCK_SCENARIO_NAME)
                .whenScenarioStateIs(WIREMOCK_GET_RESPONSE_READY_STATE)
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(RESPONSE_BODY)));

        warmUpWireMock();
    }

    private void warmUpWireMock() {
        // to warm-up wiremock so requests are returned without any delay and we could measure wait time
        new RestTemplate()
            .postForEntity(spineUrl + SCR_SPINE_ENDPOINT, null, String.class);
        new RestTemplate()
            .getForEntity(spineUrl + SCR_SPINE_CONTENT_ENDPOINT, String.class);
        wireMockServer.resetRequests();
        wireMockServer.resetScenarios();
    }
}
