package uk.nhs.adaptors.scr.component;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.nhs.adaptors.scr.WireMockInitializer;

import java.nio.file.Files;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ContextConfiguration(initializers = {WireMockInitializer.class})
public class AcsTest {
    private static final String ACS_SET_RESOURCES_ENDPOINT = "/summary-care-record/consent";
    private static final String ACS_SPINE_ENDPOINT = "/acs";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WireMockServer wireMockServer;

    @Value("classpath:acs.consent.json")
    private Resource acsSetResourceRequest;

    @Value("${spine.url}")
    private String spineUrl;

    @Value("classpath:acs.set-resource.xml")
    private Resource acsSetRequest;

    @AfterEach
    public void afterEach() {
        this.wireMockServer.resetAll();
    }

    @Test
    public void whenPostingAcsSetResourceThenExpect200() throws Exception {
        wireMockServer.stubFor(
            WireMock.post(ACS_SPINE_ENDPOINT)
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody("response")));

        String requestBody = readString(acsSetResourceRequest.getFile().toPath(), UTF_8);
        mockMvc.perform(
            post(ACS_SET_RESOURCES_ENDPOINT)
                .contentType(APPLICATION_JSON_VALUE)
                .content(requestBody))
            .andExpect(status().isOk());

        wireMockServer.verify(1, postRequestedFor(urlEqualTo(ACS_SPINE_ENDPOINT)));

        List<LoggedRequest> requests = wireMockServer.findAll(RequestPatternBuilder.allRequests());

        var request = requests.get(0);

        assertThat(request.getBodyAsString())
            .isEqualTo(Files.readString(acsSetRequest.getFile().toPath(), Charsets.UTF_8));
    }
}