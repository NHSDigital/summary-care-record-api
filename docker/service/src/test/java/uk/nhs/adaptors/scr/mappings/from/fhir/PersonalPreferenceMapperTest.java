package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.PersonalPreference;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PersonalPreferenceMapperTest extends BaseFhirMapperUtilities {

    @InjectMocks
    private PersonalPreferenceMapper personalPreferenceMapper;

    private static final String ID = "0F5A9E73-8F89-11EA-8B2D-B741F13EFC47";
    private static final String RESOURCE_DIRECTORY = "personal_preference";
    private static final String STATUS_CODE = "completed";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var observation = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);

        returnExpectedUuid(ID);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        assertThat(result.getIdRoot()).isEqualTo(ID);

    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var observation = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        assertThat(result.getCodeCode()).isEqualTo("1240651000000109");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("SARS-CoV-2 (severe acute respiratory syndrome coronavirus 2) vaccination declined");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var observation = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow() {
        var observation = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");

    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
        var observation = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);

        returnExpectedUuid(ID);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        var gpSummary = new GpSummary();
        var personalPreference = new ArrayList<PersonalPreference>();
        personalPreference.add(result);
        gpSummary.setPersonalPreferences(personalPreference);

        var personalPreferenceTemplate = TemplateUtils.loadPartialTemplate("PersonalPreferences.mustache");

        var resultStr = TemplateUtils.fillTemplate(personalPreferenceTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
