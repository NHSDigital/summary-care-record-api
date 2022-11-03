package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Investigation;
import uk.nhs.adaptors.scr.models.xml.Problem;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import uk.nhs.utils.InvestigationMapperArgumentsProvider;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class ProblemTest {
    private ProblemMapper problemMapper;

    private FhirParser fhirParser = new FhirParser();

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var fileName = "example-1";
        var json = readResourceFile(String.format("problem/%s.json", fileName));

        var condition = fhirParser.parseResource(json, Condition.class);

        var result = problemMapper.mapProblem(condition);

        assertThat(result.getIdRoot()).isEqualTo("0F5A9E75-8F89-11EA-8B2D-B741F13EFC47");
    }
//
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
        var fileName = "example-1";
        var expectedHtml = readResourceFile(String.format("problem/%s.html", fileName));
        var json = readResourceFile(String.format("problem/%s.json", fileName));

        var condition = fhirParser.parseResource(json, Condition.class);

        var result = problemMapper.mapProblem(condition);

        var problemTemplate = TemplateUtils.loadPartialTemplate("Problem.mustache");

        var resultStr = TemplateUtils.fillTemplate(problemTemplate, result);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
