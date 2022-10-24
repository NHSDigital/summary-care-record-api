package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Diagnosis;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import uk.nhs.utils.DiagnosisMapperArgumentsProvider;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class DiagnosesMapperTest {

    @InjectMocks
    private DiagnosesMapper diagnosesMapper;

    @Mock
    private UuidWrapper uuid;

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = readResourceFile(String.format("diagnosis/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("AF0AAF00-797C-11EA-B378-F1A7EC384595");

        var condition = fhirParser.parseResource(json, Condition.class);

        var result = diagnosesMapper.mapDiagnosis(condition);

        assertThat(result.getIdRoot()).isEqualTo("AF0AAF00-797C-11EA-B378-F1A7EC384595");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_Code(String fileName) {
        var json = readResourceFile(String.format("diagnosis/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("AF0AAF00-797C-11EA-B378-F1A7EC384595");

        var condition = fhirParser.parseResource(json, Condition.class);

        var result = diagnosesMapper.mapDiagnosis(condition);

        assertThat(result.getCodeCode()).isEqualTo("1300721000000109");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("COVID-19 confirmed by laboratory test");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
        var json = readResourceFile(String.format("diagnosis/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("AF0AAF00-797C-11EA-B378-F1A7EC384595");

        var condition = fhirParser.parseResource(json, Condition.class);

        var result = diagnosesMapper.mapDiagnosis(condition);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
        var expectedHtml = readResourceFile(("diagnosis/expected/expected.html"));
        var json = readResourceFile(String.format("diagnosis/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("AF0AAF00-797C-11EA-B378-F1A7EC384595");

        var condition = fhirParser.parseResource(json, Condition.class);

        var result = diagnosesMapper.mapDiagnosis(condition);

        var gpSummary = new GpSummary();
        var conditions = new ArrayList<Diagnosis>();
        conditions.add(result);
        gpSummary.setDiagnoses(conditions);

        var conditionsTemplate = TemplateUtils.loadPartialTemplate("Diagnoses.mustache");

        var resultStr = TemplateUtils.fillTemplate(conditionsTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }


}
