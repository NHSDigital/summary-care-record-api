package uk.nhs.adaptors.scr.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.clients.SpineHttpClient;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.controllers.FhirMediaTypes;

import java.nio.charset.Charset;

import static java.util.Arrays.stream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
public class GetScrServiceTest {

    private static final String NHS_NUMBER = "1234567890";
    private static final String NHS_ASID = "23456";
    private static final String CLIENT_IP = "192.168.1.1";
    private static final String BASE_URL = "http://scr.nhs.uk";

    private static final String[] IGNORED_JSON_PATHS = new String[]{
        "id",
        "entry[*].fullUrl",
        "entry[*].resource.subject.reference",
        "entry[*].resource.id"
    };

    @Value("classpath:responses/event-list-query/successResponse.xml")
    private Resource eventListQuerySuccessResponse;

    @Value("classpath:responses/event-query/successResponse.xml")
    private Resource eventQuerySuccessResponse;

    @Value("classpath:responses/event-list-query/getScrIdResponse.json")
    private Resource getScrIdResponse;

    @Value("classpath:responses/event-query/getScrResponse.json")
    private Resource getScrResponse;

    @Autowired
    private GetScrService getScrService;

    @MockBean
    private SpineClient spineClient;

    @Autowired
    private FhirParser fhirParser;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        when(spineClient.sendGetScrId(any(), any())).thenReturn(SpineHttpClient.Response.builder()
            .statusCode(HttpStatus.OK.value())
            .body(IOUtils.toString(eventListQuerySuccessResponse.getInputStream(), Charset.defaultCharset()))
            .build());
        when(spineClient.sendGetScr(any(), any())).thenReturn(SpineHttpClient.Response.builder()
            .statusCode(HttpStatus.OK.value())
            .body(IOUtils.toString(eventQuerySuccessResponse.getInputStream(), Charset.defaultCharset()))
            .build());
    }

    @SneakyThrows
    @Test
    void getScrId() {
        var bundle = getScrService.getScrId(NHS_NUMBER, NHS_ASID, CLIENT_IP, BASE_URL);
        var actualJson = fhirParser.encodeResource(FhirMediaTypes.APPLICATION_FHIR_JSON, bundle);
        var expectedJson = IOUtils.toString(getScrIdResponse.getInputStream(), Charset.defaultCharset());

        assertFhirEqual(expectedJson, actualJson, IGNORED_JSON_PATHS);
    }

    @SneakyThrows
    @Test
    void getScr() {
        var bundle = getScrService.getScr(NHS_NUMBER, NHS_ASID, CLIENT_IP, BASE_URL);
        var actualJson = fhirParser.encodeResource(FhirMediaTypes.APPLICATION_FHIR_JSON, bundle);
        var expectedJson = IOUtils.toString(getScrResponse.getInputStream(), Charset.defaultCharset());

        assertFhirEqual(expectedJson, actualJson, IGNORED_JSON_PATHS);
    }

    @SneakyThrows
    private void assertFhirEqual(String expected, String actual, String... ignoredPaths) {
        var customizations = stream(ignoredPaths)
            .map(jsonPath -> new Customization(jsonPath, (o1, o2) -> true))
            .toArray(Customization[]::new);
        JSONAssert.assertEquals(expected, actual,
            new CustomComparator(STRICT, customizations));
    }
}
