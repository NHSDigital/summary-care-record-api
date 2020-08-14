package uk.nhs.adaptors.scr.utils.spine.mock;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static io.restassured.RestAssured.given;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.utils.EndpointMockData;
import uk.nhs.adaptors.scr.utils.SpineRequest;
import uk.nhs.adaptors.scr.utils.spine.mock.interfaces.SpineMockSetupForHttpMethod;
import uk.nhs.adaptors.scr.utils.spine.mock.interfaces.SpineMockSetupWithHttpStatusCode;
import uk.nhs.adaptors.scr.utils.spine.mock.interfaces.SpineMockSetupWithResponseContent;

@Component
public class SpineMockSetupEndpoint {
    private static final String SETUP_ENDPOINT = "/setup";
    private static final int PORT = 8081;

    @Autowired
    private SpineConfiguration spineConfiguration;

    public SpineMockSetupForHttpMethod forUrl(String url) {
        Builder builder = new Builder();
        builder.url = url;
        return builder;
    }

    private static class Builder implements SpineMockSetupWithHttpStatusCode, SpineMockSetupForHttpMethod,
        SpineMockSetupWithResponseContent {
        private String url;
        private String httpMethod;
        private Integer httpStatusCode;
        private String responseContent;

        @Override
        public SpineMockSetupWithHttpStatusCode forHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        @Override
        public SpineMockSetupWithResponseContent withHttpStatusCode(Integer httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
            return this;
        }

        @Override
        public void withResponseContent(String responseContent) {
            this.responseContent = responseContent;
            setupEndpoint();
        }

        private void setupEndpoint() {
            EndpointMockData endpointMockData = new EndpointMockData();
            endpointMockData.setUrl(url);
            endpointMockData.setHttpMethod(httpMethod);
            endpointMockData.setHttpStatusCode(httpStatusCode);
            endpointMockData.setResponseContent(responseContent);

            try {
                given()
                    .port(PORT)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(new ObjectMapper().writeValueAsString(endpointMockData))
                    .when()
                    .post(SETUP_ENDPOINT)
                    .then()
                    .statusCode(OK.value())
                    .extract();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private RestTemplate prepareRestTemplate() {
        var restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(spineConfiguration.getUrl()));
        return restTemplate;
    }

    public SpineRequest getLatestRequest() {
        return prepareRestTemplate().getForEntity("latest-request", SpineRequest.class).getBody();
    }
}
