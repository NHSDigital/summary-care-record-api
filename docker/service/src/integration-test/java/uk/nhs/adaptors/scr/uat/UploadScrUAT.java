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
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.consts.ScrHttpHeaders;
import uk.nhs.adaptors.scr.consts.SpineHttpHeaders;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.UploadScrBadRequest;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.UploadScrNoConsent;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.UploadScrSuccess;
import uk.nhs.adaptors.scr.uat.common.TestData;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.CONTENT_LOCATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.SOAP_ACTION;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class UploadScrUAT {

    private static final String FHIR_ENDPOINT = "/Bundle";
    private static final String SCR_SPINE_CONTENT_ENDPOINT = "/content";
    private static final int INITIAL_WAIT_TIME = 1;
    private static final String NHSD_ASID = "123";
    private static final String NHSD_IDENTITY = randomUUID().toString();
    private static final String NHSD_SESSION_URID = "43543673484";
    private static final String CLIENT_IP = "192.168.0.24";
    private static final String EVENT_LIST_QUERY_HEADER = "urn:nhs:names:services:psisquery/QUPC_IN180000SM04";
    private static final String UPLOAD_SCR_HEADER = "urn:nhs:names:services:psis/REPC_IN150016SM05";

    @Value("classpath:uat/responses/polling/success.xml")
    private Resource pollingSuccessResponse;

    @Value("classpath:uat/responses/event-list-query/success.xml")
    private Resource eventListQuerySuccessResponse;

    @Value("classpath:uat/responses/event-list-query/noConsent.xml")
    private Resource eventListQueryNoConsentResponse;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private SpineConfiguration spineConfiguration;

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(UploadScrSuccess.class)
    void testTranslatingFromFhirToHL7v3(TestData testData) throws Exception {
        stubSpineUploadScrEndpoint();
        stubSpinePollingEndpoint();
        stubSpinePsisEndpoint(eventListQuerySuccessResponse);

        var mvcResult = mockMvc
            .perform(post(FHIR_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .header(ScrHttpHeaders.NHSD_ASID, NHSD_ASID)
                .header(ScrHttpHeaders.CLIENT_IP, CLIENT_IP)
                .header(ScrHttpHeaders.NHSD_IDENTITY, NHSD_IDENTITY)
                .header(ScrHttpHeaders.NHSD_SESSION_URID, NHSD_SESSION_URID)
                .content(testData.getFhirRequest()))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(UploadScrNoConsent.class)
    void testTranslatingFromFhirToHL7v3NoConsent(TestData testData) throws Exception {
        stubSpineUploadScrEndpoint();
        stubSpinePollingEndpoint();
        stubSpinePsisEndpoint(eventListQueryNoConsentResponse);

        var mvcResult = mockMvc
            .perform(post(FHIR_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .header(ScrHttpHeaders.NHSD_ASID, NHSD_ASID)
                .header(ScrHttpHeaders.CLIENT_IP, CLIENT_IP)
                .header(ScrHttpHeaders.NHSD_IDENTITY, NHSD_IDENTITY)
                .header(ScrHttpHeaders.NHSD_SESSION_URID, NHSD_SESSION_URID)
                .content(testData.getFhirRequest()))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isForbidden())
            .andExpect(content().json(testData.getFhirResponse()));
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(UploadScrBadRequest.class)
    void testTranslatingFromFhirToHL7v3InvalidRequest(TestData testData) throws Exception {
        var mvcResult = mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .header(ScrHttpHeaders.NHSD_ASID, NHSD_ASID)
                .header(ScrHttpHeaders.CLIENT_IP, CLIENT_IP)
                .header(ScrHttpHeaders.NHSD_IDENTITY, NHSD_IDENTITY)
                .header(ScrHttpHeaders.NHSD_SESSION_URID, NHSD_SESSION_URID)
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
            WireMock.post(spineConfiguration.getPsisQueriesEndpoint())
                .withHeader(SOAP_ACTION, equalTo(EVENT_LIST_QUERY_HEADER))
                .withHeader(CONTENT_TYPE, equalTo(TEXT_XML_VALUE))
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(readString(response.getFile().toPath(), UTF_8))));
    }

    private void stubSpineUploadScrEndpoint() {
        wireMockServer.stubFor(
            WireMock.post(spineConfiguration.getScrEndpoint())
                .withHeader(SpineHttpHeaders.NHSD_SESSION_URID, equalTo(NHSD_SESSION_URID))
                .withHeader(SpineHttpHeaders.NHSD_ASID, equalTo(NHSD_ASID))
                .withHeader(SOAP_ACTION, equalTo(UPLOAD_SCR_HEADER))
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader(CONTENT_LOCATION, SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader(RETRY_AFTER, String.valueOf(INITIAL_WAIT_TIME))));
    }

    private void stubSpinePollingEndpoint() throws IOException {
        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT)
                .withHeader(SpineHttpHeaders.NHSD_SESSION_URID, equalTo(NHSD_SESSION_URID))
                .withHeader(SpineHttpHeaders.NHSD_ASID, equalTo(NHSD_ASID))
                .willReturn(aResponse()
                    .withBody(readString(pollingSuccessResponse.getFile().toPath(), UTF_8))
                    .withStatus(OK.value())));
    }
}
