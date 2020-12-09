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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class SendUAT {

    private static final String FHIR_ENDPOINT = "/Bundle";
    private static final String SPINE_SCR_ENDPOINT = "/clinical";
    private static final String SCR_SPINE_CONTENT_ENDPOINT = "/content";
    private static final int INITIAL_WAIT_TIME = 1;
    private static final String NHSD_ASID = "123";

    @Value("classpath:responses/polling/success.xml")
    private Resource pollingSuccessResponse;

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
        wireMockServer.stubFor(
            WireMock.post(SPINE_SCR_ENDPOINT)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(INITIAL_WAIT_TIME))));
        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT)
                .willReturn(aResponse()
                    .withBody(readString(pollingSuccessResponse.getFile().toPath(), UTF_8))
                    .withStatus(OK.value())));

        var mvcResult = mockMvc
            .perform(post(FHIR_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .header("Nhsd-Asid", NHSD_ASID)
                .content(testData.getFhirRequest()))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(content().json(testData.getFhirResponse()))
            .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(CustomArgumentsProvider.UploadScrInvalid.class)
    void testTranslatingFromFhirToHL7v3InvalidRequest(String category, TestData testData) throws Exception {
        var mvcResult = mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType(APPLICATION_FHIR_JSON_VALUE)
                .header("Nhsd-Asid", NHSD_ASID)
                .content(testData.getFhirRequest()))
            .andExpect(request().asyncStarted())
            .andExpect(request().asyncResult(notNullValue()))
            .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(testData.getFhirResponse()));
    }
}
