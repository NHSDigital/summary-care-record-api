package uk.nhs.adaptors.scr.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.config.SdsConfiguration;
import java.net.URISyntaxException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.lenient;

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
