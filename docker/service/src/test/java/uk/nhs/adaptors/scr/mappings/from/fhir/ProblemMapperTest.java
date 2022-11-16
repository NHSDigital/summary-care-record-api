package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class ProblemMapperTest extends BaseFhirMapperTest {

    @InjectMocks
    private ProblemMapper problemMapper;

    private final String RESOURCE_DIRECTORY = "problem";
    private final String BASIC_FILE_NAME = "example-1";
    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var condition = getFileAsObject(RESOURCE_DIRECTORY, BASIC_FILE_NAME, Condition.class);

        var result = problemMapper.mapProblem(condition);

        assertThat(result.getIdRoot()).isEqualTo("BB890EB6-3152-4D08-9331-D48FE63198C1");
    }

//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(InvestigationMapperArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_Code(String fileName) {
//        var json = readResourceFile(String.format("problem/%s.json", fileName));
//
//        var procedure = fhirParser.parseResource(json, Procedure.class);
//
//        var result = problemMapper.mapProblem(procedure);
//
//        assertThat(result.getCodeCode()).isEqualTo("1240461000000109");
//        assertThat(result.getCodeDisplayName())
//            .isEqualTo("Measurement of severe acute respiratory syndrome coronavirus 2 antibody (procedure)");
//    }
//
//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(InvestigationMapperArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
//        var json = readResourceFile(String.format("problem/%s.json", fileName));
//
//        var procedure = fhirParser.parseResource(json, Procedure.class);
//
//        var result = problemMapper.mapProblem(procedure);
//
//        assertThat(result.getStatusCodeCode()).isEqualTo("normal");
//    }
//
//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(InvestigationMapperArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
//        var json = readResourceFile(String.format("problem/%s.json", fileName));
//
//        var procedure = fhirParser.parseResource(json, Procedure.class);
//
//        var result = problemMapper.mapProblem(procedure);
//
//        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
//    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, BASIC_FILE_NAME);
        var condition = getFileAsObject(RESOURCE_DIRECTORY, BASIC_FILE_NAME, Condition.class);

        var result = problemMapper.mapProblem(condition);

        var problemTemplate = TemplateUtils.loadPartialTemplate("Problem.mustache");

        var resultStr = TemplateUtils.fillTemplate(problemTemplate, result);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
