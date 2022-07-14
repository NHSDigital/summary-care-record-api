package uk.nhs.adaptors.scr.services;

import org.apache.http.client.utils.URIBuilder;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.clients.sds.SdsClient;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.config.SdsConfiguration;

import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
class SdsServiceTest {

    private static final String SDS_BASE_URL = "localhost:test";
    private static final String PR_PATH = "/PractitionerRole";
    private static final String USER_ROLE_QUERY_NAME = "user-role-id";
    private static final String USER_ROLE_FHIR_IDENTIFIER = "https://fhir.nhs.uk/Id/nhsJobRoleCode";

    private static final String NHSD_SESSION_URID = "555021935107";
    private static final String ROLE_CODE = "S0030:G0100:R0570";

    private static final String NHSD_SESSION_URID_2 = "655021935106";
    private static final String ROLE_CODE_2 = "S0032:G0102:R0572";

    private FhirParser fhirParser = new FhirParser();

    @Mock
    private SdsClient sdsClient;

    @Mock
    private SdsConfiguration sdsConfiguration;

    @InjectMocks
    private SdsService sdsService;

    @BeforeEach
    void setUp() {
        lenient().when(sdsConfiguration.getBaseUrl()).thenReturn(SDS_BASE_URL);
    }

    @Test
    public void whenGetUserRoleCodeExpectHappyPath() throws URISyntaxException {


        var fileName = "sds_role_response";
        var responseData = readResourceFile(String.format("sds_tests/%s.json", fileName));
        var response = fhirParser.parseResource(responseData, Bundle.class);

        when(sdsClient.sendGet(urlBuilder(NHSD_SESSION_URID))).thenReturn(response);

        var result = sdsService.getUserRoleCode(NHSD_SESSION_URID);

        assertNotEquals(NHSD_SESSION_URID, result);
        assertEquals(ROLE_CODE, result);

    }

    @Test
    public void whenGetUserRoleCodeExpectCorrectUriAccessed() throws URISyntaxException {

        var fileName = "sds_role_response_2";
        var responseData = readResourceFile(String.format("sds_tests/%s.json", fileName));
        var response = fhirParser.parseResource(responseData, Bundle.class);
        when(sdsClient.sendGet(urlBuilder(NHSD_SESSION_URID_2))).thenReturn(response);

        var result = sdsService.getUserRoleCode(NHSD_SESSION_URID_2);

        assertNotEquals(NHSD_SESSION_URID_2, result);
        assertEquals(ROLE_CODE_2, result);
        verify(sdsClient, never()).sendGet(urlBuilder(NHSD_SESSION_URID));
    }

    @Test
    public void whenGetUserRoleCodeAndPractionerRoleResourceEmptyExpectNoRoleCode() throws URISyntaxException {

        var fileName = "sds_role_response_empty_entry";
        var responseData = readResourceFile(String.format("sds_tests/%s.json", fileName));
        var response = fhirParser.parseResource(responseData, Bundle.class);
        when(sdsClient.sendGet(urlBuilder(NHSD_SESSION_URID))).thenReturn(response);

        var result = sdsService.getUserRoleCode(NHSD_SESSION_URID);

        assertEquals("", result);
    }

    private URI urlBuilder(String nhsdSessionUrid) throws URISyntaxException {
        var userRoleId = USER_ROLE_FHIR_IDENTIFIER + "|" + nhsdSessionUrid;

        return new URIBuilder(SDS_BASE_URL + PR_PATH)
            .setScheme("http")
            .addParameter(USER_ROLE_QUERY_NAME, userRoleId)
            .build();
    }
}
