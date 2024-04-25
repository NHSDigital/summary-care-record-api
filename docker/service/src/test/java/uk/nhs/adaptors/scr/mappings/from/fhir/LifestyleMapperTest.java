package uk.nhs.adaptors.scr.mappings.from.fhir;

import java.util.ArrayList;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Lifestyle;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test runner for Lifestyle HL7 XML files' conversion to HL7-XML.
 *
 * @see: NIAD-2325
 */
@ExtendWith(MockitoExtension.class)
public class LifestyleMapperTest extends BaseFhirMapperUtilities {

    private static final String RESOURCE_DIRECTORY = "lifestyle";
    private static final String STATUS_CODE = "final";
    private static final String FILE_NAME = "without_author";
    private static final String FILE_NAME_HL7 = "without_author";

    @InjectMocks
    private LifestyleMapper mapper;

    /**
     * Testing the root id value.
     */
    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = mapper.map(obj);
        assertThat(result.getIdRoot()).isEqualTo("5EDDDF8C-775A-4437-8990-41012DB32BD0");
    }

    /**
     * Testing the code values.
     */
    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = mapper.map(obj);

        assertThat(result.getCodeCode()).isEqualTo("102906002");
        assertThat(result.getCodeDisplayName()).isEqualTo("delinquent behaviour");
    }

    /**
     * Testing the status code.
     */
    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = mapper.map(obj);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    /**
     * Testing the effective time value.
     */
    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTime() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = mapper.map(obj);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("19900825");
    }

    /**
     * Test the expected output matches the actual.
     */
    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME_HL7);
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);

        // Map using method in fromFIHR CareProfessionalDocumentationMapper::map().
        var result = mapper.map(communication);
        var gpSummary = new GpSummary();
        var list = new ArrayList<Lifestyle>();
        list.add(result);
        gpSummary.setLifestyles(list);

        // Assert that the expected HLS from the mustache template matches the expected, removing whitespace.
        var template = TemplateUtils.loadPartialTemplate("Lifestyles.mustache");
        var resultStr = TemplateUtils.fillTemplate(template, gpSummary);
        assertThat(resultStr).isEqualToIgnoringWhitespace(expectedHtml);
    }
}
