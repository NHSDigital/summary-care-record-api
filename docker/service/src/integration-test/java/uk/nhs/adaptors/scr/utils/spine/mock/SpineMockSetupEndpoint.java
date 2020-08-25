package uk.nhs.adaptors.scr.utils.spine.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.utils.EndpointMockData;
import uk.nhs.adaptors.scr.utils.SpineRequest;
import uk.nhs.adaptors.scr.utils.spine.mock.interfaces.SpineMockSetupForHttpMethod;
import uk.nhs.adaptors.scr.utils.spine.mock.interfaces.SpineMockSetupForPathMethod;
import uk.nhs.adaptors.scr.utils.spine.mock.interfaces.SpineMockSetupWithHttpStatusCode;
import uk.nhs.adaptors.scr.utils.spine.mock.interfaces.SpineMockSetupWithResponseContent;

import static io.restassured.RestAssured.given;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class SpineMockSetupEndpoint {
    private static final String SETUP_ENDPOINT = "/setup";
    private static final String RESET_ENDPOINT = SETUP_ENDPOINT + "/reset";

    @Autowired
    private SpineConfiguration spineConfiguration;

    public SpineMockSetupForPathMethod onMockServer(String mockServerBaseUri) {
        return new Builder(mockServerBaseUri);
    }

    public void reset(String mockServerBaseUri) {
        given()
            .baseUri(mockServerBaseUri)
            .post(RESET_ENDPOINT)
            .then()
            .statusCode(OK.value());
    }

    private static final class Builder implements
        SpineMockSetupWithHttpStatusCode,
        SpineMockSetupForHttpMethod,
        SpineMockSetupWithResponseContent,
        SpineMockSetupForPathMethod {

        private String mockServerBaseUri;

        private String path;
        private String httpMethod;
        private Integer httpStatusCode;
        private String responseContent;

        private Builder(String mockServerBaseUri) {
            this.mockServerBaseUri = mockServerBaseUri;
        }

        @Override
        public SpineMockSetupForHttpMethod forPath(String path) {
            this.path = path;
            return this;
        }

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
            endpointMockData.setUrl(path);
            endpointMockData.setHttpMethod(httpMethod);
            endpointMockData.setHttpStatusCode(httpStatusCode);
            endpointMockData.setResponseContent(responseContent);

            try {
                given()
                    .baseUri(this.mockServerBaseUri)
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
