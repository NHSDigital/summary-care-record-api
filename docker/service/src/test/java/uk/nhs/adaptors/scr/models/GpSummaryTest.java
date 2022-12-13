package uk.nhs.adaptors.scr.models;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.models.xml.Treatment;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class GpSummaryTest {
    private static final String RESOURCE_DIRECTORY = "gp_summary";
    private static final String HTML_RESOURCE_DIRECTORY = "gp_summary/from/fhir";
    private static final String STANDARD_FILE_NAME = "standard-gp-summary";
    private static final String ADDITIONAL_INFO_FILE_NAME_1 = "additional-information-gp-summary-1";
    private static final String NHSD_ASID = "1029384756";
    private FhirParser fhirParser = new FhirParser();

    @Test
    public void When_MappingStandardGpSummaryFromBundle_Expect_PresentationTextHL7Match() {
        var valueFile = "standard-gp-summary-presentation-text-value";
        var expectedPresentationValue = readResourceFile(String.format(HTML_RESOURCE_DIRECTORY + "/%s.html", valueFile));

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", STANDARD_FILE_NAME));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getCompositionDate()).isEqualToIgnoringWhitespace("20201117010000");
        assertThat(result.getPresentation().getPresentationText()).isEqualToIgnoringWhitespace(expectedPresentationValue);
    }

    @Test
    public void When_MappingAdditionalInfoGpSummaryFromBundle_Expect_PresentationTextHL7Match() {
        var valueFile = "additional-information-gp-summary-1-presentation-text-value";
        var expectedPresentationValue = readResourceFile(String.format(HTML_RESOURCE_DIRECTORY + "/%s.html", valueFile));

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", ADDITIONAL_INFO_FILE_NAME_1));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getCompositionDate()).isEqualToIgnoringWhitespace("20200430171300");
        assertThat(result.getPresentation().getPresentationText()).isEqualToIgnoringWhitespace(expectedPresentationValue);
    }

    @Test
    public void When_MappingAdditionalInfoGpSummaryFromBundle_Expect_Treatments() {

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", ADDITIONAL_INFO_FILE_NAME_1));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getTreatments()).hasAtLeastOneElementOfType(Treatment.class);
        

    }

    @Test
    public void When_MappingStandardGpSummaryFromBundle_Expect_HL7Match() {
        var expectedHtml = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.html", STANDARD_FILE_NAME));

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", STANDARD_FILE_NAME));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        var gpSummaryTemplate = TemplateUtils.loadPartialTemplate("GpSummary.mustache");

        var resultStr = TemplateUtils.fillTemplate(gpSummaryTemplate, result);
        assertThat(resultStr).isEqualToIgnoringWhitespace(expectedHtml);
    }

    @Test
    public void When_MappingAdditionalInfoGpSummaryFromBundle_Expect_HL7Match() {
        var expectedHtml = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.html", ADDITIONAL_INFO_FILE_NAME_1));

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", ADDITIONAL_INFO_FILE_NAME_1));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        var gpSummaryTemplate = TemplateUtils.loadPartialTemplate("GpSummary.mustache");

        var resultStr = TemplateUtils.fillTemplate(gpSummaryTemplate, result);
        assertThat(resultStr).isEqualToIgnoringWhitespace(expectedHtml);
    }
}
