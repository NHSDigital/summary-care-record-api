package uk.nhs.adaptors.scr;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;

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

import com.google.common.base.Charsets;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@ExtendWith({SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AcsTest {
    private static final String ACS_ENDPOINT = "/summary-care-record/consent";

    @Autowired
    private MockMvc mockMvc;

    @Value("classpath:acs.consent.json")
    private Resource simpleJson;

    @Test
    public void whenPostingFhirJsonThenExpect200() throws Exception {
        String requestBody = Files.readString(simpleJson.getFile().toPath(), Charsets.UTF_8);
        mockMvc.perform(
            post(ACS_ENDPOINT)
                .contentType(APPLICATION_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isOk());
    }
}
