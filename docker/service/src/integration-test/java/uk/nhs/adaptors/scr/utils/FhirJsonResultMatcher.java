package uk.nhs.adaptors.scr.utils;

import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.test.web.servlet.ResultMatcher;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

public class FhirJsonResultMatcher {
    public static ResultMatcher fhirJson(String jsonContent, String... ignoredPaths) {
        return result -> {
            var customizations = stream(ignoredPaths)
                .map(jsonPath -> new Customization(jsonPath, (o1, o2) -> true))
                .toArray(Customization[]::new);
            String content = result.getResponse().getContentAsString(UTF_8);
            JSONAssert.assertEquals(jsonContent, content,
                new CustomComparator(STRICT, customizations));
        };
    }
}
