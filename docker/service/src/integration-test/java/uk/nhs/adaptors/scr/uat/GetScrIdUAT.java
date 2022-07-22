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
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.nhs.adaptors.scr.WireMockInitializer;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.consts.ScrHttpHeaders;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.GetScrIdNoConsent;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.GetScrIdNotFound;
import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider.GetScrIdSuccess;
import uk.nhs.adaptors.scr.uat.common.TestData;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.nhs.adaptors.scr.consts.SpineHttpHeaders.SOAP_ACTION;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.scr.utils.FhirJsonResultMatcher.fhirJson;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class GetScrIdUAT {

    private static final String EVENT_LIST_QUERY_HEADER = "urn:nhs:names:services:psisquery/QUPC_IN180000SM04";
    private static final String GET_SCR_ID_ENDPOINT = "/DocumentReference";
    private static final String NHS_NUMBER = "https://fhir.nhs.uk/Id/nhs-number|9995000180";
    private static final String SORT_PARAM = "date";
    private static final String COUNT_PARAM = "1";
    private static final String TYPE_PARAM = "http://snomed.info/sct|196981000000101";
    private static final String NHSD_ASID = "1029384756";
    private static final String CLIENT_IP = "192.168.0.24";
    private static final String[] IGNORED_JSON_PATHS = new String[]{
        "id",
        "entry[*].fullUrl",
        "entry[*].resource.subject.reference",
        "entry[*].resource.id"
    };

    @Value("classpath:uat/responses/event-list-query/success.xml")
    private Resource eventListQuerySuccessResponse;

    @Value("classpath:uat/responses/event-list-query/noConsent.xml")
    private Resource eventListQueryNoConsentResponse;

    @Value("classpath:uat/responses/event-list-query/notFound.xml")
    private Resource eventListQueryNotFoundResponse;

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
//
//    @ParameterizedTest(name = "[{index}] - {0}")
//    @ArgumentsSource(GetScrIdSuccess.class)
//    void testRetrieveLatestScrId(TestData testData) throws Exception {
//        stubSpinePsisEndpoint(eventListQuerySuccessResponse);
//
//        performRequestAndAssert(testData, OK);
//    }
//
//    @ParameterizedTest(name = "[{index}] - {0}")
//    @ArgumentsSource(GetScrIdNotFound.class)
//    void testRetrieveLatestScrIdNoResults(TestData testData) throws Exception {
//        stubSpinePsisEndpoint(eventListQueryNotFoundResponse);
//
//        performRequestAndAssert(testData, OK);
//    }
//
//    @ParameterizedTest(name = "[{index}] - {0}")
//    @ArgumentsSource(GetScrIdNoConsent.class)
//    void testRetrieveLatestScrIdNoConsent(TestData testData) throws Exception {
//        stubSpinePsisEndpoint(eventListQueryNoConsentResponse);
//
//        performRequestAndAssert(testData, FORBIDDEN);
//    }

//    private void performRequestAndAssert(TestData testData, HttpStatus expectedHttpStatus) throws Exception {
//        mockMvc.perform(get(GET_SCR_ID_ENDPOINT)
//            .contentType(APPLICATION_FHIR_JSON_VALUE)
//            .header(ScrHttpHeaders.NHSD_ASID, NHSD_ASID)
//            .header(ScrHttpHeaders.CLIENT_IP, CLIENT_IP)
//            .queryParam("patient", NHS_NUMBER)
//            .queryParam("type", TYPE_PARAM)
//            .queryParam("_sort", SORT_PARAM)
//            .queryParam("_count", COUNT_PARAM))
//            .andExpect(status().is(expectedHttpStatus.value()))
//            .andExpect(fhirJson(testData.getFhirResponse(), IGNORED_JSON_PATHS));
//    }

    private void stubSpinePsisEndpoint(Resource response) throws IOException {
        wireMockServer.stubFor(
            WireMock.post(spineConfiguration.getPsisQueriesEndpoint())
                .withHeader(SOAP_ACTION, equalTo(EVENT_LIST_QUERY_HEADER))
                .withHeader(CONTENT_TYPE, equalTo(TEXT_XML_VALUE))
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(readString(response.getFile().toPath(), UTF_8))));
    }
}
