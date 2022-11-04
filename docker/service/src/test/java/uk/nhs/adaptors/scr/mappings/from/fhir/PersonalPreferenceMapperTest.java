package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.PersonalPreference;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import uk.nhs.utils.PersonalPreferencesMapperArgumentsProvider;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class PersonalPreferenceMapperTest {

    @InjectMocks
    private PersonalPreferenceMapper personalPreferenceMapper;

    @Mock
    private UuidWrapper uuid;

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PersonalPreferencesMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = readResourceFile(String.format("personal_preference/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F5A9E73-8F89-11EA-8B2D-B741F13EFC47");

        var observation = fhirParser.parseResource(json, Observation.class);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        assertThat(result.getIdRoot()).isEqualTo("0F5A9E73-8F89-11EA-8B2D-B741F13EFC47");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PersonalPreferencesMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_Code(String fileName) {
        var json = readResourceFile(String.format("personal_preference/%s.json", fileName));

        var observation = fhirParser.parseResource(json, Observation.class);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        assertThat(result.getCodeCode()).isEqualTo("1240651000000109");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("SARS-CoV-2 (severe acute respiratory syndrome coronavirus 2) vaccination declined");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PersonalPreferencesMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
        var json = readResourceFile(String.format("personal_preference/%s.json", fileName));

        var observation = fhirParser.parseResource(json, Observation.class);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        assertThat(result.getStatusCodeCode()).isEqualTo("completed");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PersonalPreferencesMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
        var json = readResourceFile(String.format("personal_preference/%s.json", fileName));

        var observation = fhirParser.parseResource(json, Observation.class);

        var result = personalPreferenceMapper.mapPersonalPreference(observation);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PersonalPreferencesMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
        var expectedHtml = readResourceFile(String.format("personal_preference/%s.html", fileName));
        var json = readResourceFile(String.format("personal_preference/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F5A9E73-8F89-11EA-8B2D-B741F13EFC47");

        var observation = fhirParser.parseResource(json, Observation.class);

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
