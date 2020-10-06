package uk.nhs.adaptors.scr.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

@Component
@Slf4j
public class WiremockInitializer implements InitializingBean {

    public static final String WIREMOCK_SCENARIO_NAME = "POST + polling GET";
    public static final String WIREMOCK_GET_RESPONSE_READY_STATE = "GET response ready";
    private static final String SCR_SPINE_CONTENT_ENDPOINT = "/content";
    private static final String RESPONSE_BODY = "response-body";

    @Value("${spine.url}")
    private String spineUrl;

    @Value("${embeddedFakeSpine.enabled}")
    private boolean enabled;

    @Value("${embeddedFakeSpine.port}")
    private int port;

    @Value("${embeddedFakeSpine.pollingInitialWaitTime}")
    private int initialWaitTime;

    @Value("${embeddedFakeSpine.pollingWaitTime}")
    private int waitTime;

    @Value("${spine.acsEndpoint}")
    private String acsEndpoint;

    @Value("${spine.scrEndpoint}")
    private String scrEndpoint;

    @Autowired
    private Environment environment;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (enabled) {
            LOGGER.warn("EMBEDDED WIREMOCK ENABLED! DON'T USE ON PRODUCTION!!!");
            setUpWireMock();
        }
    }

    private void setUpWireMock() {
        WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().port(port));
        wireMockServer.start();

        setUpScrMappings(wireMockServer);
        setUpAcsMappings(wireMockServer);
    }

    private void setUpAcsMappings(WireMockServer wireMockServer) {
        LOGGER.info("Stubbing ACS endpoints on embedded WireMock server");
        wireMockServer.stubFor(
            WireMock.post(acsEndpoint)
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody("response")));
    }

    private void setUpScrMappings(WireMockServer wireMockServer) {
        LOGGER.info("Stubbing SCR endpoints on embedded WireMock server");
        wireMockServer.stubFor(
            WireMock.post(scrEndpoint).inScenario(WIREMOCK_SCENARIO_NAME)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", "http://localhost:" + port + SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(initialWaitTime))));
        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT).inScenario(WIREMOCK_SCENARIO_NAME)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                    .withStatus(ACCEPTED.value())
                    .withHeader("Content-Location", spineUrl + SCR_SPINE_CONTENT_ENDPOINT)
                    .withHeader("Retry-After", String.valueOf(waitTime)))
                .willSetStateTo(WIREMOCK_GET_RESPONSE_READY_STATE));
        wireMockServer.stubFor(
            WireMock.get(SCR_SPINE_CONTENT_ENDPOINT).inScenario(WIREMOCK_SCENARIO_NAME)
                .whenScenarioStateIs(WIREMOCK_GET_RESPONSE_READY_STATE)
                .willReturn(aResponse()
                    .withStatus(OK.value())
                    .withBody(RESPONSE_BODY)));
    }
}
