package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.ScrGatewayTimeoutException;
import uk.nhs.adaptors.scr.services.ScrService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
@DirtiesContext
@Slf4j
class FhirControllerTest {

    private static final String FHIR_ENDPOINT = "/fhir";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FhirParser fhirParser;
    @MockBean
    private ScrService scrService;

    @Test
    void whenScrServiceThrowsScrGatewayTimeoutExpect504() throws Exception {
        when(fhirParser.parseResource(any(), any())).thenReturn(new Bundle());
        doThrow(new ScrGatewayTimeoutException("")).when(scrService).handleFhir(any());

        mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+json")
                .content("test data"))
            .andExpect(status().isGatewayTimeout());
    }
}
