package uk.nhs.adaptors.scr.mappings.from.fhir;

import java.util.ArrayList;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.CareProfessionalDocumentation;
import uk.nhs.adaptors.scr.models.xml.Lifestyle;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test runner for Lifestyle HL7 XML files' conversion to HL7-XML.
 *
 * @see: NIAD-2325
 */
@ExtendWith(MockitoExtension.class)
public class LifestyleMapperTest extends BaseFhirMapperTest {

    private static final String RESOURCE_DIRECTORY = "lifestyle";
    private static final String STATUS_CODE = "normal";
    private static final String FILE_NAME = "example";

    @InjectMocks
    private LifestyleMapper mapper;

    /**
     * Testing the root id value.
     */
    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = mapper.map(obj);
        assertThat(result.getIdRoot()).isEqualTo("7D50E3C0-7565-11E8-AEC7-950876D8FD27");
    }

    /**
     * Testing the code values.
     */
    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = mapper.map(obj);

        assertThat(result.getCodeCode()).isEqualTo("308452008");
        assertThat(result.getCodeDisplayName()).isEqualTo("Referral to speech and language therapist");
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

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20180621161151");
    }

    /**
     * Test the expected output matches the actual.
     */
    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
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
