package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DiagnosesMapperTest extends BaseHL7MapperUtilities {

    @InjectMocks
    private DiagnosesMapper diagnosesMapper;

    private static final String PERTINENT_INFORMATION_BASE_PATH = "//QUPC_IN210000UK04/ControlActEvent/subject//"
        + "GPSummary/pertinentInformation2/pertinentCREType[.//UKCT_MT144042UK01.Diagnosis]";
    private static final String ID = "AF0AAF00-797C-11EA-B378-F1A7EC384595";
    private static final String FILE_NAME = "example";
    private static final String RESOURCE_DIRECTORY = "diagnosis";


    @Test
    public void When_MappingFromHl7_Expect_GetId() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        var result = diagnosesMapper.map(html);

        assertThat(result.get(0).getId()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        diagnosesMapper.map(html);

        verifyXmlUtilsHits(html, PERTINENT_INFORMATION_BASE_PATH);
    }

    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        diagnosesMapper.map(html);

        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromHl7_Expect_DateTimeFormatted() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        var result = diagnosesMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getOnsetDateTimeType().toHumanDisplay()).isEqualTo("2020-08-05");
    }

    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        var result = diagnosesMapper.map(html);

        var resultEncounter = (Condition) result.get(0);
        var codingFirstRep = resultEncounter.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1300721000000109");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("COVID-19 confirmed by laboratory test");
    }

    @Test
    public void When_MappingFromHl7_Expect_ClinicalStatusMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        var result = diagnosesMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getCode()).isEqualTo("active");

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getSystem())
            .isEqualTo("http://hl7.org/fhir/ValueSet/condition-clinical");
        // this is not working due to it not being mapped + url is different in map class

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getDisplay()).isEqualTo("Active");
    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);

        var result = diagnosesMapper.map(html);

        var actualJson = encodeToJson(result.get(0));

        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson.trim());
    }

}
