package uk.nhs.adaptors.scr;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
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

import java.nio.file.Files;

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

    @Value("classpath:simple.fhir.json")
    private Resource simpleFhirJson;

    @Value("classpath:simple.fhir.xml")
    private Resource simpleFhirXml;

    @Test
    public void whenGetHealthCheck_expect200() throws Exception {
        mockMvc.perform(get(HEALTHCHECK_ENDPOINT))
            .andExpect(status().isOk());
    }

    @Test
    public void whenPostingFhirJson_expect200() throws Exception {
        String requestBody = Files.readString(simpleFhirJson.getFile().toPath(), Charsets.UTF_8);
        mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+json")
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    public void whenPostingFhirXml_expect200() throws Exception {
        String requestBody = Files.readString(simpleFhirXml.getFile().toPath(), Charsets.UTF_8);
        mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+xml")
                .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    public void whenUnableToParseData_expect400() throws Exception {
        //TODO: assert response is OperationOutcome
        mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+json")
                .content("qwe"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(
            post(FHIR_ENDPOINT)
                .contentType("application/fhir+xml")
                .content("qwe"))
            .andExpect(status().isBadRequest());
    }
}
