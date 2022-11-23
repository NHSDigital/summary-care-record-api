package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.SocialOrPersonalCircumstance;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import uk.nhs.utils.FindingMapperArgumentsProvider;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class SocialOrPersonalCircumstanceMapperTest {

    @InjectMocks
    private SocialOrPersonalCircumstanceMapper mapper;

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = readResourceFile(String.format("social_personal_circumstances/%s.json", fileName));
        var observation = fhirParser.parseResource(json, Observation.class);
        var result = mapper.map(observation);

        assertThat(result.getIdRoot()).isEqualTo("E619AE11-4417-11E9-9B19-B5A20573E36F");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_Code(String fileName) {
        var json = readResourceFile(String.format("social_personal_circumstances/%s.json", fileName));
        var observation = fhirParser.parseResource(json, Observation.class);
        var result = mapper.map(observation);

        assertThat(result.getCodeCode()).isEqualTo("970531000000100");
        assertThat(result.getCodeDisplayName()).isEqualTo("Main spoken language Cornish");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
        var json = readResourceFile(String.format("social_personal_circumstances/%s.json", fileName));
        var observation = fhirParser.parseResource(json, Observation.class);
        var result = mapper.map(observation);

        assertThat(result.getStatusCodeCode()).isEqualTo("normal");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
        var json = readResourceFile(String.format("social_personal_circumstances/%s.json", fileName));
        var observation = fhirParser.parseResource(json, Observation.class);
        var result = mapper.map(observation);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20190311155807");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
        var json = readResourceFile(String.format("social_personal_circumstances/%s.json", fileName));
        var observation = fhirParser.parseResource(json, Observation.class);
        var result = mapper.map(observation);
        var gpSummary = new GpSummary();
        var list = new ArrayList<SocialOrPersonalCircumstance>();

        list.add(result);
        gpSummary.setSocialOrPersonalCircumstances(list);

        var template = TemplateUtils.loadPartialTemplate("SocialOrPersonalCircumstances.mustache");
        var resultStr = TemplateUtils.fillTemplate(template, gpSummary);
        var expectedHtml = readResourceFile("social_personal_circumstances/example.html");
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
