package uk.nhs.adaptors.scr.clients;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.nhs.adaptors.scr.clients.spine.SpineClient;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient;
import uk.nhs.adaptors.scr.clients.spine.SpineStringResponseHandler;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.NoSpineResultException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;
import uk.nhs.adaptors.scr.models.ProcessingResult;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpineClientTest {

    private static final String SPINE_URL = "https://spine";
    private static final String CONTENT_LOCATION = "/content-location";
    private static final String SCR_ENDPOINT = "/scr";
    private static final String SOAP_ENVELOPE = "<soap:Envelope>envelope_data</soap:Envelope>";
    private static final String HL7 = "<hl7:MCCI_IN010000UK13>hl7</hl7:MCCI_IN010000UK13>";
    private static final String RESPONSE_BODY = SOAP_ENVELOPE + HL7;
    private static final String ACS_REQUEST_BODY = "some_acs_body";
    private static final String NHSD_ASID = "123123";
    private static final String PARTY_TO_ID = "spine_party_key";
    private static final String NHSD_IDENTITY = randomUUID().toString();
    private static final String NHSD_SESSION_URID = "937463642332";

    @Mock
    private SpineConfiguration spineConfiguration;
    @Mock
    private ScrConfiguration scrConfiguration;
    @Mock
    private SpineHttpClient spineHttpClient;
    @Mock
    private SpineStringResponseHandler stringResponseHandler;

    @InjectMocks
    private SpineClient spineClient;

    @BeforeEach
    void setUp() {
        lenient().when(scrConfiguration.getPartyIdTo()).thenReturn(PARTY_TO_ID);
        lenient().when(spineConfiguration.getUrl()).thenReturn(SPINE_URL);
    }

    @Test
    void whenSendingScrReturns202ExpectResult() {
        when(spineConfiguration.getScrEndpoint()).thenReturn(SCR_ENDPOINT);
        var headers = new Header[]{
            new BasicHeader("Header-Name", "headerValue")
        };
        when(spineHttpClient.sendRequest(any(HttpPost.class), eq(stringResponseHandler)))
            .thenReturn(new SpineHttpClient.Response(HttpStatus.ACCEPTED.value(), headers, RESPONSE_BODY));

        var response = spineClient.sendScrData(ACS_REQUEST_BODY, NHSD_ASID, NHSD_IDENTITY, NHSD_SESSION_URID);

        var httpPostArgumentCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(spineHttpClient).sendRequest(httpPostArgumentCaptor.capture(), eq(stringResponseHandler));

        var httpPost = httpPostArgumentCaptor.getValue();
        assertThat(httpPost.getURI().toString()).isEqualTo(SPINE_URL + SCR_ENDPOINT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED.value());
        assertThat(response.getHeaders()).isEqualTo(headers);
        assertThat(response.getBody()).isEqualTo(RESPONSE_BODY);
    }

    @Test
    void whenSendingScrReturnsNon202ExpectResult() {
        when(spineConfiguration.getScrEndpoint()).thenReturn(SCR_ENDPOINT);
        when(spineHttpClient.sendRequest(any(HttpPost.class), eq(stringResponseHandler)))
            .thenReturn(new SpineHttpClient.Response(HttpStatus.OK.value(), new Header[0], RESPONSE_BODY));

        assertThatThrownBy(() -> spineClient.sendScrData(ACS_REQUEST_BODY, NHSD_ASID, NHSD_IDENTITY, NHSD_SESSION_URID))
            .isExactlyInstanceOf(UnexpectedSpineResponseException.class);
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void whenGetScrProcessingResultReturnsWithOneRetryWithinTimeExpectResult() {
        when(spineConfiguration.getScrResultRepeatTimeout()).thenReturn(500L);

        var postResponseHeaders = new Header[]{
            new BasicHeader("Retry-After", "50")
        };

        when(spineHttpClient.sendRequest(any(), eq(stringResponseHandler)))
            .thenReturn(new SpineHttpClient.Response(HttpStatus.ACCEPTED.value(), postResponseHeaders, null))
            .thenReturn(new SpineHttpClient.Response(HttpStatus.OK.value(), new Header[0], RESPONSE_BODY));

        var result = spineClient.getScrProcessingResult(CONTENT_LOCATION, 50, NHSD_ASID, NHSD_IDENTITY,
            NHSD_SESSION_URID);

        var requestArgumentCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(spineHttpClient, times(2)).sendRequest(requestArgumentCaptor.capture(), eq(stringResponseHandler));

        assertRequestUri(requestArgumentCaptor.getAllValues());
        assertThat(result).isEqualTo(new ProcessingResult().setSoapEnvelope(SOAP_ENVELOPE).setHl7(HL7));
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void whenGetScrProcessingResultReachesRepeatTimeoutExpectException() {
        when(spineConfiguration.getScrResultRepeatTimeout()).thenReturn(500L);

        when(spineHttpClient.sendRequest(any(), eq(stringResponseHandler)))
            .thenReturn(new SpineHttpClient.Response(
                HttpStatus.ACCEPTED.value(),
                new Header[]{
                    new BasicHeader("Retry-After", String.valueOf(200))
                },
                null));

        assertThatThrownBy(() -> spineClient.getScrProcessingResult(CONTENT_LOCATION, 100, NHSD_ASID, NHSD_IDENTITY, NHSD_SESSION_URID))
            .isExactlyInstanceOf(NoSpineResultException.class)
            .hasMessage("Spine polling yield no result");

        var requestArgumentCaptor = ArgumentCaptor.forClass(HttpGet.class);
        verify(spineHttpClient, atLeast(2)).sendRequest(requestArgumentCaptor.capture(), eq(stringResponseHandler));
        assertRequestUri(requestArgumentCaptor.getAllValues());
    }

    private void assertRequestUri(List<HttpGet> requests) {
        assertThat(!requests.isEmpty());
        assertThat(requests.stream()
            .map(HttpRequestBase::getURI)
            .map(URI::toString)
            .collect(Collectors.toList()))
            .allMatch(url -> url.equals(SPINE_URL + CONTENT_LOCATION));
    }
}
