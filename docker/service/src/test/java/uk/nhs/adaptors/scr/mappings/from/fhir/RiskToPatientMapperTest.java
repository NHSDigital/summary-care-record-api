package uk.nhs.adaptors.scr.mappings.from.fhir;

import java.util.ArrayList;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.RiskToPatient;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class RiskToPatientMapperTest extends BaseFhirMapperTest {

    private static final String RESOURCE_DIRECTORY = "risk_to_patient";
    private static final String STATUS_CODE = "final";
    private static final String FILE_NAME = "example";

    @InjectMocks
    private RiskToPatientMapper mapper;

    /**
     * Testing the root id value.
     */
    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = mapper.map(obj);
        assertThat(result.getIdRoot()).isEqualTo("10CE2F30-8AF5-11EA-9FCE-AFDCAECF9DFB");
    }

    /**
     * Testing the code values.
     */
    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = mapper.map(obj);
        var displayString = "Close exposure to severe acute respiratory syndrome coronavirus 2 infection (event)";
        assertThat(result.getCodeCode()).isEqualTo("1240441000000108");
        assertThat(result.getCodeDisplayName()).isEqualTo(displayString);
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

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }

    /**
     * Test the expected output matches the actual.
     */
    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
        var observation = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);

        // Map using method in fromFIHR RiskToPatientMapper::map().
        var result = mapper.map(observation);
        var gpSummary = new GpSummary();
        var list = new ArrayList<RiskToPatient>();
        list.add(result);
        gpSummary.setRisksToPatient(list);

        // Assert that the expected HLS from the mustache template matches the expected, removing whitespace.
        var template = TemplateUtils.loadPartialTemplate("RisksToPatient.mustache");
        var resultStr = TemplateUtils.fillTemplate(template, gpSummary);
        assertThat(resultStr.replaceAll("\\s+", "")).isEqualTo(expectedHtml.replaceAll("\\s+", ""));
    }
}
