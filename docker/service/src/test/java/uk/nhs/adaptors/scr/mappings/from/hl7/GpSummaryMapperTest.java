package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class GpSummaryMapperTest extends BaseHL7MapperTest{
    @InjectMocks
    private GpSummaryMapper gpSummaryMapper;

    private static final String RESOURCE_DIRECTORY = "gp_summary";
    private static final String STANDARD_FILE_NAME = "standard-gp-summary";
    private static final String NHSD_ASID = "1029384756";
    private FhirParser fhirParser = new FhirParser();

    @Test
    public void When_MappingGpSummaryFromHL7_Expect_BundleMatch() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, STANDARD_FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, STANDARD_FILE_NAME);

        // act
        var results = gpSummaryMapper.map(html);

        var resultBundle = new Bundle();
        results.stream().map(resource -> getBundleEntryComponent(resource)).forEach(resultBundle::addEntry);

        var resultStr = fhirParser.encodeToJson(resultBundle);

        assertThat(resultStr).isEqualToIgnoringWhitespace(expectedJson);
    }
}
