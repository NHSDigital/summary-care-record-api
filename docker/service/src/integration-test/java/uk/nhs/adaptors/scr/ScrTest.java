package uk.nhs.adaptors.scr;

import com.google.common.base.Charsets;

import ca.uhn.fhir.context.FhirContext;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.components.FhirParser;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ExtendWith({SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ScrTest {
    private static final String HEALTHCHECK_ENDPOINT = "/healthcheck";
    private static final String FHIR_ENDPOINT = "/fhir";

    @Autowired
    private MockMvc mockMvc;

    @Value("classpath:bundle.fhir.json")
    private Resource simpleFhirJson;

    @Value("classpath:bundle.fhir.xml")
    private Resource simpleFhirXml;

    @Autowired
    private FhirParser fhirParser;

    @Test
    public void whenGetHealthCheckThenExpect200() throws Exception {
        mockMvc.perform(get(HEALTHCHECK_ENDPOINT))
            .andExpect(status().isOk());
    }

    @Test
    public void whenPostingFhirJsonThenExpect200() throws Exception {
        String requestBody = Files.readString(simpleFhirJson.getFile().toPath(), Charsets.UTF_8);
        mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+json")
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    public void whenPostingFhirXmlThenExpect200() throws Exception {
        String requestBody = Files.readString(simpleFhirXml.getFile().toPath(), Charsets.UTF_8);
        mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+xml")
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    public void whenUnableToParseJsonDataThenExpect400() throws Exception {
        MvcResult result = mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+json")
                .content("qwe"))
            .andExpect(status().isBadRequest())
            .andReturn();

        FhirContext ctx = FhirContext.forR4();
        String responseBody = result.getResponse().getContentAsString();
        var response = ctx.newJsonParser().parseResource(responseBody);

        assertTrue(response instanceof OperationOutcome);
    }

    @Test
    public void whenUnableToParseXmlDataThenExpect400() throws Exception {
        MvcResult result = mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+xml")
                .content("qwe"))
            .andExpect(status().isBadRequest())
            .andReturn();

        FhirContext ctx = FhirContext.forR4();
        String responseBody = result.getResponse().getContentAsString();
        var response = ctx.newXmlParser().parseResource(responseBody);

        assertTrue(response instanceof OperationOutcome);
    }
}
