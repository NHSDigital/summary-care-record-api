package uk.nhs.adaptors.scr.models;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class GpSummaryTest {
    private static final String RESOURCE_DIRECTORY = "gp_summary";
    private static final String STANDARD_FILE_NAME = "standard-gp-summary";
    private static final String NHSD_ASID = "1029384756";
    private FhirParser fhirParser = new FhirParser();

    @Test
    public void When_MappingGpSummaryFromBundle_Expect_HL7Match() {
        var expectedHtml = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.html", STANDARD_FILE_NAME));

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", STANDARD_FILE_NAME));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        var gpSummaryTemplate = TemplateUtils.loadPartialTemplate("GpSummary.mustache");

        var resultStr = TemplateUtils.fillTemplate(gpSummaryTemplate, result);
        assertThat(resultStr).isEqualToIgnoringWhitespace(expectedHtml);
    }
}
