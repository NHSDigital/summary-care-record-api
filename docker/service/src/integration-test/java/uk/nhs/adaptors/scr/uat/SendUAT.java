//package uk.nhs.adaptors.scr.uat;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.NotImplementedException;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ArgumentsSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import uk.nhs.adaptors.scr.IntegrationTestsExtension;
//import uk.nhs.adaptors.scr.uat.common.CustomArgumentsProvider;
//import uk.nhs.adaptors.scr.uat.common.TestData;
//import uk.nhs.adaptors.scr.utils.spine.mock.SpineMockSetupEndpoint;
//
//import static org.hamcrest.Matchers.notNullValue;
//import static org.springframework.http.HttpStatus.ACCEPTED;
//import static org.springframework.http.HttpStatus.OK;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ExtendWith({SpringExtension.class, IntegrationTestsExtension.class})
//@DirtiesContext
//@Slf4j
//public class SendUAT {
//
//    private static final String FHIR_ENDPOINT = "/fhir";
//    private static final String SPINE_SCR_ENDPOINT = "/summarycarerecord";
//    private static final String REQUEST_IDENTIFIER = "123";
//
//    @Value("${spine.url}")
//    private String spineUrl;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private SpineMockSetupEndpoint spineMockSetupEndpoint;
//
//    @ParameterizedTest(name = "[{index}] - {0}")
//    @ArgumentsSource(CustomArgumentsProvider.Outbound.class)
//    void testTranslatingFromFhirToHL7v3(String category, TestData testData) throws Exception {
//        spineMockSetupEndpoint
//            .onMockServer(spineUrl)
//            .forPath(SPINE_SCR_ENDPOINT)
//            .forHttpMethod("POST")
//            .withHttpStatusCode(ACCEPTED.value())
//            .withResponseContent("response");
//        spineMockSetupEndpoint
//            .onMockServer(spineUrl)
//            .forPath(SPINE_SCR_ENDPOINT + "/" + REQUEST_IDENTIFIER)
//            .forHttpMethod("GET")
//            .withHttpStatusCode(OK.value())
//            .withResponseContent("response");
//
//        MvcResult mvcResult = mockMvc
//            .perform(post(FHIR_ENDPOINT)
//                .contentType(getContentType(testData.getFhirFormat()))
//                .content(testData.getFhir()))
//            .andExpect(request().asyncStarted())
//            .andExpect(request().asyncResult(notNullValue()))
//            .andExpect(status().isOk())
//            .andReturn();
//
//        mockMvc.perform(asyncDispatch(mvcResult));
//    }
//
//    private String getContentType(TestData.FhirFormat fhirFormat) {
//        switch (fhirFormat) {
//            case JSON:
//                return "application/fhir+json";
//            case XML:
//                return "application/fhir+xml";
//            default:
//                throw new NotImplementedException();
//        }
//    }
//}
