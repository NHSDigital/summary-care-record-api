package uk.nhs.adaptors.scr.controllers.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.adaptors.scr.controllers.utils.UrlUtils.extractBaseUrl;

class UrlUtilsTest {

    private static final String URL_NO_QUERY_PARAMS = "https://internal-dev.api.service.nhs.uk/summary-care-record"
        + "/DocumentReference";
    private static final String URL_WITH_QUERY_PARAMS = "https://internal-dev.api.service.nhs.uk/summary-care-record"
        + "/DocumentReference?patient=123";
    private static final String REQUEST_URI = "/DocumentReference";

    @ParameterizedTest
    @ValueSource(strings = {URL_NO_QUERY_PARAMS, URL_WITH_QUERY_PARAMS})
    void whenExtractBaseUrlExpectCorrectBase(String url) {
        String base = extractBaseUrl(url, REQUEST_URI);
        assertThat(base).isEqualTo("https://internal-dev.api.service.nhs.uk/summary-care-record");
    }
}
