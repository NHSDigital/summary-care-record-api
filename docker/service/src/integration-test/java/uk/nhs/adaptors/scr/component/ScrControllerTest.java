package uk.nhs.adaptors.scr.component;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.scr.clients.SpineClientContract;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.services.UploadScrService;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
public class ScrControllerTest {
    private static final String FHIR_ENDPOINT = "/Bundle";
    private static final String REQUEST_BODY = "something";
    private static final int LONG_INITIAL_WAIT_TIME = 1000;
    private static final long SHORT_RESULT_TIMEOUT = 10;
    private static final String FHIR_JSON_CONTENT_TYPE = "application/fhir+json";
    private static final String NHSD_ASID = "123";
    private static final String CLIENT_IP = "192.168.0.24";

    @LocalServerPort
    private int port;

    @MockBean
    private FhirParser fhirParser;

    @MockBean
    private UploadScrService uploadScrService;

    @MockBean
    private SpineConfiguration spineConfiguration;

    @MockBean
    private ScrConfiguration scrConfiguration;

    @MockBean
    private SpineClientContract spineClient;

    @Test
    public void whenRequestProcessingTakesTooMuchTimeExpect504() {
        when(spineConfiguration.getEndpointCert()).thenReturn("some_cert");
        when(scrConfiguration.getPartyIdFrom()).thenReturn("some-party-from");
        when(scrConfiguration.getPartyIdTo()).thenReturn("some-party-to");
        when(scrConfiguration.getNhsdAsidTo()).thenReturn("some-asid-to");

        when(fhirParser.parseResource(any(), any())).thenReturn(new Bundle());
        doAnswer(invocation -> {
            try {
                Thread.sleep(LONG_INITIAL_WAIT_TIME);
            } catch (InterruptedException ex) {
                return null;
            }
            return null;
        }).when(uploadScrService).uploadScr(any());

        when(spineConfiguration.getScrResultTimeout()).thenReturn(SHORT_RESULT_TIMEOUT);

        given()
            .port(port)
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .header("Nhsd-Asid", NHSD_ASID)
            .header("client-ip", CLIENT_IP)
            .body(REQUEST_BODY)
            .when()
            .post(FHIR_ENDPOINT)
            .then()
            .contentType(FHIR_JSON_CONTENT_TYPE)
            .statusCode(GATEWAY_TIMEOUT.value());
    }
}
