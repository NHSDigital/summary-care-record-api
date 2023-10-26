package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AuthorMapperTest extends BaseFhirMapperTest {

    @InjectMocks
    private AuthorMapper authorMapper;

    private static final String RESOURCE_DIRECTORY = "author";
    private static final String AUTHOR_BUNDLE_FILE = "authorBundle";
    private static final String AUTHOR_HL7_FILE = "author";

    //TODO: This test maps a gpSummary from the bundle (including author) and compares it to the template.
    @Test
    public void When_MappingFromAuthorlessFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, AUTHOR_HL7_FILE);
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, AUTHOR_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        authorMapper.mapAuthor(gpSummary, jsonBundle);

        //TODO: This template is large, and perhaps unnecessary for Author only?
        var findingsTemplate = TemplateUtils.loadPartialTemplate("GpSummary.mustache");

        var resultStr = TemplateUtils.fillTemplate(findingsTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }

}
