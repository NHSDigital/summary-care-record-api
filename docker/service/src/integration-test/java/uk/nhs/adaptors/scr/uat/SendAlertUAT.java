package uk.nhs.adaptors.scr.uat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.nhs.adaptors.scr.WireMockInitializer;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.consts.ScrHttpHeaders;
import uk.nhs.adaptors.scr.consts.SpineHttpHeaders;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import static java.util.UUID.randomUUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class SendAlertUAT {
    private static final String NHSD_ASID = "54343566";
    private static final String NHSD_IDENTITY = randomUUID().toString();
    private static final String NHSD_SESSION_URID = "43543673484";
    private static final String CORRELATION_ID = randomUUID().toString();
    private static final String REQUEST_ID = randomUUID().toString();
    private static final String CLIENT_IP = "192.168.0.24";
    private static final String ALERT_ENDPOINT = "/AuditEvent";

    @Value("classpath:uat/responses/alert/error.json")
    private Resource alertErrorResponse;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private SpineConfiguration spineConfiguration;

    /*@ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(SendAlertSuccess.class)
    public void testSendAlertSuccess(TestData testData) throws Exception {
        stubSpineAlertEndpoint(OK, null);

        performRequest(testData.getFhirRequest())
            .andExpect(status().isCreated());
    }*/

    /*@ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(SendAlertSpineError.class)
    public void testSendAlertError(TestData testData) throws Exception {
        stubSpineAlertEndpoint(BAD_REQUEST, readString(alertErrorResponse.getFile().toPath(), UTF_8));

        performRequest(testData.getFhirRequest())
            .andExpect(status().isBadRequest())
            .andExpect(content().json(testData.getFhirResponse()));
    }*/

    /*@ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(SendAlertBadRequest.class)
    public void testSendAlertBadRequest(TestData testData) throws Exception {
        performRequest(testData.getFhirRequest())
            .andExpect(status().isBadRequest())
            .andExpect(content().json(testData.getFhirResponse()));
    }*/

    private ResultActions performRequest(String body) throws Exception {
        return mockMvc.perform(post(ALERT_ENDPOINT)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .header(ScrHttpHeaders.NHSD_ASID, NHSD_ASID)
            .header(ScrHttpHeaders.NHSD_IDENTITY, NHSD_IDENTITY)
            .header(ScrHttpHeaders.NHSD_SESSION_URID, NHSD_SESSION_URID)
            .header(ScrHttpHeaders.CLIENT_IP, CLIENT_IP)
            .header(ScrHttpHeaders.CORRELATION_ID, CORRELATION_ID)
            .header(ScrHttpHeaders.REQUEST_ID, REQUEST_ID)
            .content(body));

    }

    private void stubSpineAlertEndpoint(HttpStatus responseStatus, String response) throws IOException {
        wireMockServer.stubFor(
            WireMock.post(spineConfiguration.getAlertEndpoint())
                .withHeader(SpineHttpHeaders.NHSD_SESSION_URID, equalTo(NHSD_SESSION_URID))
                .withHeader(SpineHttpHeaders.NHSD_IDENTITY, equalTo(NHSD_IDENTITY))
                .withHeader(SpineHttpHeaders.NHSD_ASID, equalTo(NHSD_ASID))
                .withHeader(SpineHttpHeaders.NHSD_CORRELATION_ID, equalTo(CORRELATION_ID))
                .withHeader(SpineHttpHeaders.NHSD_REQUEST_ID, equalTo(REQUEST_ID))
                .willReturn(aResponse()
                    .withBody(response)
                    .withStatus(responseStatus.value())));
    }
}
