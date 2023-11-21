package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.FamilyHistory;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FamilyHistoryMapperTest extends BaseFhirMapperTest {

    private static final String RESOURCE_DIRECTORY = "family_history";
    private static final String STATUS_CODE = "final";
    private static final String FILE_NAME = "without_author";
    private static final String FILE_NAME_HL7 = "without_author";

    @InjectMocks
    private FamilyHistoryMapper familyHistoryMapper;

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = familyHistoryMapper.map(obj);
        assertThat(result.getIdRoot()).isEqualTo("51089E5B-0840-4237-8D91-CFC0238E83B4");
    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = familyHistoryMapper.map(obj);

        assertThat(result.getCodeCode()).isEqualTo("289916006");
        assertThat(result.getCodeDisplayName()).isEqualTo("family history of kidney disease");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = familyHistoryMapper.map(obj);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTime() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = familyHistoryMapper.map(obj);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("19900825");
    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME_HL7);
        var observation = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);

        // Map using method in fromFIHR CareProfessionalDocumentationMapper::map().
        var result = familyHistoryMapper.map(observation);
        var gpSummary = new GpSummary();
        var list = new ArrayList<FamilyHistory>();
        list.add(result);
        gpSummary.setFamilyHistories(list);

        // Assert that the expected HLS from the mustache template matches the expected, removing whitespace.
        var template = TemplateUtils.loadPartialTemplate("FamilyHistories.mustache");
        var resultStr = TemplateUtils.fillTemplate(template, gpSummary);
        assertThat(resultStr).isEqualToIgnoringWhitespace(expectedHtml);
    }

    /**
     * Added tests to validate what happens when partial dates are received in the SCR.
     * See XMLToFhirMapper/FhirParser for details.
     */
    @Test
    public void When_MappingFromFHIRWithDifferentDateFormats_Expect_MatchingHtml() {
        var expectedHtmlYear = getExpectedHtml(RESOURCE_DIRECTORY, "date_formats");
        var observationYear = getFileAsObject(RESOURCE_DIRECTORY, "year_only", Observation.class);
        var observationYearMonth = getFileAsObject(RESOURCE_DIRECTORY, "year_and_month", Observation.class);
        var observationYearMonthDash = getFileAsObject(RESOURCE_DIRECTORY, "year_and_month_dash", Observation.class);

        // Map using method in fromFIHR CareProfessionalDocumentationMapper::map().
        var result = familyHistoryMapper.map(observationYear);
        var gpSummary = new GpSummary();
        var list = new ArrayList<FamilyHistory>();
        list.add(result);
        result = familyHistoryMapper.map(observationYearMonth);
        list.add(result);
        result = familyHistoryMapper.map(observationYearMonthDash);
        list.add(result);

        gpSummary.setFamilyHistories(list);

        // Assert that the expected HLS from the mustache template matches the expected, removing whitespace.
        var template = TemplateUtils.loadPartialTemplate("FamilyHistories.mustache");
        var resultStr = TemplateUtils.fillTemplate(template, gpSummary);
        assertThat(resultStr).isEqualToIgnoringWhitespace(expectedHtmlYear);


    }
}
