package uk.nhs.adaptors.scr.uat;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.SOAP_ACTION;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.scr.utils.FhirJsonResultMatcher.fhirJson;

import java.io.IOException;

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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.WireMockInitializer;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.consts.ScrHttpHeaders;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.GetScrInitialUploadSuccess;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.GetScrNoConsent;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.GetScrSuccess;
import uk.nhs.adaptors.scr.uat.common.TestData;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class GetScrUAT {
    private static final String EVENT_LIST_QUERY_HEADER = "urn:nhs:names:services:psisquery/QUPC_IN180000SM04";
    private static final String EVENT_QUERY_HEADER = "urn:nhs:names:services:psisquery/QUPC_IN190000UK04";
    private static final String GET_SCR_ENDPOINT = "/Bundle";
    private static final String COMPOSITION =
        "FA60BE64-1F34-11EB-A2A8-000C29A364EB$composition.subject:Patient.identifier=https://fhir.nhs.uk/Id/nhs-number|9995000180";
    private static final String NHSD_ASID = "7695489345";
    private static final String CLIENT_IP = "192.168.0.24";
    private static final String[] IGNORED_JSON_PATHS = new String[]{
        "id",
        "entry[*].fullUrl",
        "entry[*].resource.subject.reference",
        "entry[*].resource.id",
        "entry[*].resource.encounter.reference",
        "entry[*].resource.practitioner.reference",
        "entry[*].resource.participant[*].individual.reference",
        "entry[*].resource.organization.reference",
        "entry[*].resource.owner.reference",
        "entry[*].resource.author[*].reference",
        "entry[*].resource.patient.reference"
    };

    @Value("classpath:uat/responses/event-list-query/success.xml")
    private Resource eventListQuerySuccessResponse;

    @Value("classpath:uat/responses/event-list-query/noConsent.xml")
    private Resource eventListQueryNoConsentResponse;

    @Value("classpath:uat/responses/event-query/success.xml")
    private Resource eventQuerySuccessResponse;

    @Value("classpath:uat/responses/event-query/initial-upload-success.xml")
    private Resource eventQueryInitialUploadSuccessResponse;

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
    @ArgumentsSource(GetScrNoConsent.class)
    void testGetScrNoConsent(TestData testData) throws Exception {
        stubSpinePsisEventListEndpoint(eventListQueryNoConsentResponse);

        performRequestAndAssert(testData);
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(GetScrSuccess.class)
    void testGetScrSuccess(TestData testData) throws Exception {
        stubSpinePsisEventListEndpoint(eventListQuerySuccessResponse);
        stubSpinePsisEventQueryEndpoint(eventQuerySuccessResponse);

        performRequestAndAssert(testData);
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(GetScrInitialUploadSuccess.class)
    void testGetScrInitialUploadSuccess(TestData testData) throws Exception {
        stubSpinePsisEventListEndpoint(eventListQuerySuccessResponse);
        stubSpinePsisEventQueryEndpoint(eventQueryInitialUploadSuccessResponse);

        performRequestAndAssert(testData);
    }

    private void performRequestAndAssert(TestData testData) throws Exception {
        mockMvc.perform(get(GET_SCR_ENDPOINT)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .header(ScrHttpHeaders.NHSD_ASID, NHSD_ASID)
            .header(ScrHttpHeaders.CLIENT_IP, CLIENT_IP)
            .queryParam("composition.identifier", COMPOSITION))
            .andExpect(status().isOk())
            .andExpect(fhirJson(String.format(testData.getFhirResponse(),
                wireMockServer.baseUrl(),
                wireMockServer.baseUrl()),
                IGNORED_JSON_PATHS)
            );
    }

    private void stubSpinePsisEventListEndpoint(Resource response) throws IOException {
        wireMockServer.stubFor(
            WireMock.post(spineConfiguration.getPsisQueriesEndpoint())
                .withHeader(SOAP_ACTION, equalTo(EVENT_LIST_QUERY_HEADER))
                .withHeader(CONTENT_TYPE, equalTo(TEXT_XML_VALUE))
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(readString(response.getFile().toPath(), UTF_8))));
    }

    private void stubSpinePsisEventQueryEndpoint(Resource response) throws IOException {
        wireMockServer.stubFor(
            WireMock.post(spineConfiguration.getPsisQueriesEndpoint())
                .withHeader(SOAP_ACTION, equalTo(EVENT_QUERY_HEADER))
                .withHeader(CONTENT_TYPE, equalTo(TEXT_XML_VALUE))
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(readString(response.getFile().toPath(), UTF_8))));
    }
}
