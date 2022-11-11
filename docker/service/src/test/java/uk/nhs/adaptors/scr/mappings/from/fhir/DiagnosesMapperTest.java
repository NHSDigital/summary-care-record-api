package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.Diagnosis;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DiagnosesMapperTest extends BaseFhirMapperTest {

    @InjectMocks
    private DiagnosesMapper diagnosesMapper;

    private static final String ID = "AF0AAF00-797C-11EA-B378-F1A7EC384595";
    private static final String RESOURCE_DIRECTORY = "diagnosis";
    private static final String RESOURCE_DIR_EXPECTED = "diagnosis/expected/";
    private static final String FILE_NAME = "example";
    private static final String FILE_NAME_EXPECTED = "expected";


    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var condition = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Condition.class);

        returnExpectedUuid(ID);

        var result = diagnosesMapper.mapDiagnosis(condition);

        assertThat(result.getIdRoot()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var condition = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Condition.class);

        returnExpectedUuid(ID);

        var result = diagnosesMapper.mapDiagnosis(condition);

        assertThat(result.getCodeCode()).isEqualTo("1300721000000109");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("COVID-19 confirmed by laboratory test");
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow() {
        var condition = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Condition.class);

        returnExpectedUuid(ID);

        var result = diagnosesMapper.mapDiagnosis(condition);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIR_EXPECTED, FILE_NAME_EXPECTED);
        var condition = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Condition.class);

        returnExpectedUuid(ID);

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
