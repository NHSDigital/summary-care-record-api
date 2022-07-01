package uk.nhs.adaptors.scr.services;

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
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient;
import uk.nhs.adaptors.scr.config.SdsConfiguration;
import uk.nhs.adaptors.scr.exceptions.NoSpineResultException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;
import uk.nhs.adaptors.scr.models.ProcessingResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SdsServiceTest {

    private static final String SDS_BASE_URL = "localhost:test";
    private static final String NHSD_SESSION_URID = "555021935107";
    private static final String ROLE_CODE = "S0030:G0100:R0570";

    private static final String NHSD_SESSION_URID_2 = "655021935106";
    private static final String ROLE_CODE_2 = "50030:G0100:R0575";

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

        var result = sdsService.getUserRoleCode(NHSD_SESSION_URID);

        assertNotEquals(NHSD_SESSION_URID, result);
        assertEquals(ROLE_CODE, result);

    }

    @Test
    public void basicTest() throws URISyntaxException {

        var result = sdsService.getUserRoleCode(NHSD_SESSION_URID_2);

        assertNotEquals(NHSD_SESSION_URID_2, result);
        assertEquals(ROLE_CODE_2, result);

    }
}
