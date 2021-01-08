package uk.nhs.adaptors.scr.uat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.nhs.adaptors.scr.WireMockInitializer;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider;
import uk.nhs.adaptors.scr.uat.common.TestData;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static java.util.Arrays.stream;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.SOAP_ACTION;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class GetScrIdUAT {

    private static final String SPINE_PSIS_ENDPOINT = "/sync-service";
    private static final String EVENT_LIST_QUERY_HEADER = "urn:nhs:names:services:psisquery/QUPC_IN180000SM04";
    private static final String GET_SCR_ID_ENDPOINT = "/DocumentReference";
    private static final String NHS_NUMBER = "https://fhir.nhs.uk/Id/nhs-number|9995000180";
    private static final String SORT_PARAM = "date";
    private static final String COUNT_PARAM = "1";
    private static final String TYPE_PARAM = "http://snomed.info/sct|196981000000101";
    private static final String NHSD_ASID = "1029384756";
    private static final String CLIENT_IP = "192.168.0.24";
    private static final String CLIENT_REQUEST_URL = "localhost:9000/DocumentReference";
    private static final String[] IGNORED_JSON_PATHS = new String[]{
        "id",
        "entry[*].fullUrl",
        "entry[*].resource.subject.reference",
        "entry[*].resource.id"
    };

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
    @ArgumentsSource(CustomArgumentsProvider.GetScrIdSuccess.class)
    void testRetrieveLatestScrId(String category, TestData testData) throws Exception {
        stubSpinePsisEndpoint(eventListQuerySuccessResponse);

        performRequestAndAssert(testData);
    }

    @ParameterizedTest(name = "[{index}] - {0}")
    @ArgumentsSource(CustomArgumentsProvider.GetScrIdNoConsent.class)
    void testRetrieveLatestScrIdNoConsent(String category, TestData testData) throws Exception {
        stubSpinePsisEndpoint(eventListQueryNoConsentResponse);

        performRequestAndAssert(testData);
    }

    private void performRequestAndAssert(TestData testData) throws Exception {
        mockMvc.perform(get(GET_SCR_ID_ENDPOINT)
            .contentType(APPLICATION_FHIR_JSON_VALUE)
            .header("Nhsd-Asid", NHSD_ASID)
            .header("client-ip", CLIENT_IP)
            .header("client-request-url", CLIENT_REQUEST_URL)
            .queryParam("patient", NHS_NUMBER)
            .queryParam("type", TYPE_PARAM)
            .queryParam("_sort", SORT_PARAM)
            .queryParam("_count", COUNT_PARAM))
            .andExpect(status().isOk())
            .andExpect(fhirJson(testData.getFhirResponse(), IGNORED_JSON_PATHS));
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

    public ResultMatcher fhirJson(String jsonContent, String... ignoredPaths) {
        return result -> {
            var customizations = stream(ignoredPaths)
                .map(jsonPath -> new Customization(jsonPath, (o1, o2) -> true))
                .toArray(Customization[]::new);
            String content = result.getResponse().getContentAsString(UTF_8);
            JSONAssert.assertEquals(jsonContent, content,
                new CustomComparator(STRICT, customizations));
        };
    }
}
