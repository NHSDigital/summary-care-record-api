package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProblemMapperTest extends BaseFhirMapperUtilities {

    @InjectMocks
    private ProblemMapper problemMapper;

    private static final String ID = "BB890EB6-3152-4D08-9331-D48FE63198C1";
    private static final String RESOURCE_DIRECTORY = "problem";
    private static final String BASIC_FILE_NAME = "example-1";
    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var condition = getFileAsObject(RESOURCE_DIRECTORY, BASIC_FILE_NAME, Condition.class);

        var result = problemMapper.mapProblem(condition);

        assertThat(result.getIdRoot()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var condition = getFileAsObject(RESOURCE_DIRECTORY, BASIC_FILE_NAME, Condition.class);

        returnExpectedUuid(ID);

        var result = problemMapper.mapProblem(condition);

        assertThat(result.getCodeCode()).isEqualTo("181301000000103");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Abstract problem node");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var condition = getFileAsObject(RESOURCE_DIRECTORY, BASIC_FILE_NAME, Condition.class);

        returnExpectedUuid(ID);

        var result = problemMapper.mapProblem(condition);

        assertThat(result.getStatusCodeCode()).isEqualTo("active");
    }

// Commented out, awaiting further information and action in NIAD-2505
//    @Test
//    public void When_MappingFromFHIR_Expect_Diagnosis() {
//        var condition = getFileAsObject(RESOURCE_DIRECTORY, BASIC_FILE_NAME, Condition.class);
//
//        returnExpectedUuid(ID);
//
//        var result = problemMapper.mapProblem(condition);
//
//        assertThat(result.getDiagnosisId()).isEqualTo("D680F6BE-73B9-4E18-988B-1D55E1B6F2D5");
//    }
//    @Test
//    public void When_MappingFromFHIR_Expect_MatchingHtml() {
//        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, BASIC_FILE_NAME);
//        var condition = getFileAsObject(RESOURCE_DIRECTORY, BASIC_FILE_NAME, Condition.class);
//
//        var result = problemMapper.mapProblem(condition);
//
//        var problemTemplate = TemplateUtils.loadPartialTemplate("Problems.mustache");
//
//        var gpSummary = new GpSummary();
//        var problems = new ArrayList<Problem>();
//        problems.add(result);
//        gpSummary.setProblems(problems);
//
//        var resultStr = TemplateUtils.fillTemplate(problemTemplate, gpSummary);
//        assertThat(resultStr).isEqualTo(expectedHtml);
//    }
}
