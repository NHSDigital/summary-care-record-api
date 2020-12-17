package uk.nhs.adaptors.scr.uat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.nhs.adaptors.scr.WireMockInitializer;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider;
import uk.nhs.adaptors.scr.uat.common.TestData;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.nhs.adaptors.scr.consts.HttpHeaders.SOAP_ACTION;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class UploadScrUAT {

    private static final String FHIR_ENDPOINT = "/Bundle";
    private static final String SPINE_SCR_ENDPOINT = "/clinical";
    private static final String SCR_SPINE_CONTENT_ENDPOINT = "/content";
    private static final int INITIAL_WAIT_TIME = 1;
    private static final String NHSD_ASID = "123";
    private static final String NHSD_IDENTITY = randomUUID().toString();
    private static final String CLIENT_IP = "192.168.0.24";
    private static final String SPINE_PSIS_ENDPOINT = "/sync-service";
    private static final String EVENT_LIST_QUERY_HEADER = "urn:nhs:names:services:psisquery/QUPC_IN180000SM04";

    @Value("classpath:responses/polling/success.xml")
    private Resource pollingSuccessResponse;

    @Value("classpath:responses/event-list-query/successResponse.xml")
    private Resource eventListQuerySuccessResponse;

    @Value("classpath:responses/event-list-query/noConsentResponse.xml")
    private Resource eventListQueryNoConsentResponse;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WireMockServer wireMockServer;

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(CustomArgumentsProvider.UploadScrSuccess.class)
    void testTranslatingFromFhirToHL7v3(String category, TestData testData) throws Exception {
        stubSpineUploadScrEndpoint();
        stubSpinePollingEndpoint();
        stubSpinePsisEndpoint(eventListQuerySuccessResponse);

        var mvcResult = mockMvc
            .perform(post(FHIR_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .header("Nhsd-Asid", NHSD_ASID)
                .header("client-ip", CLIENT_IP)
                .header("NHSD-Identity-UUID", NHSD_IDENTITY)
                .content(testData.getFhirRequest()))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(CustomArgumentsProvider.UploadScrForbidden.class)
    void testTranslatingFromFhirToHL7v3NoConsent(String category, TestData testData) throws Exception {
        stubSpineUploadScrEndpoint();
        stubSpinePollingEndpoint();
        stubSpinePsisEndpoint(eventListQueryNoConsentResponse);

        var mvcResult = mockMvc
            .perform(post(FHIR_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .header("Nhsd-Asid", NHSD_ASID)
                .header("client-ip", CLIENT_IP)
                .header("NHSD-Identity-UUID", NHSD_IDENTITY)
                .content(testData.getFhirRequest()))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isForbidden())
            .andExpect(content().json(testData.getFhirResponse()));
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(CustomArgumentsProvider.UploadScrInvalid.class)
    void testTranslatingFromFhirToHL7v3InvalidRequest(String category, TestData testData) throws Exception {
        var mvcResult = mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .header("Nhsd-Asid", NHSD_ASID)
                .header("client-ip", CLIENT_IP)
                .header("NHSD-Identity-UUID", NHSD_IDENTITY)
                .content(testData.getFhirRequest()))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(testData.getFhirResponse()));
    }

    private void stubSpinePsisEndpoint(Resource response) throws IOException {
        wireMockServer.stubFor(
            WireMock.post(SPINE_PSIS_ENDPOINT)
                .withHeader(SOAP_ACTION, equalTo(EVENT_LIST_QUERY_HEADER))
                .withHeader(CONTENT_TYPE, equalTo(TEXT_XML_VALUE))
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(readString(response.getFile().toPath(), UTF_8))));
    }

    private void stubSpineUploadScrEndpoint() {
        wireMockServer.stubFor(
            WireMock.post(SPINE_SCR_ENDPOINT)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(INITIAL_WAIT_TIME))));
    }

    private void stubSpinePollingEndpoint() throws IOException {
        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT)
                .willReturn(aResponse()
                    .withBody(readString(pollingSuccessResponse.getFile().toPath(), UTF_8))
                    .withStatus(OK.value())));
    }
}
