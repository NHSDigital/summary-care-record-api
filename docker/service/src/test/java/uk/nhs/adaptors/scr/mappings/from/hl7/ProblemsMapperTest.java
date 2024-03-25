package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProblemsMapperTest extends BaseHL7MapperTest {

    @InjectMocks
    private ProblemsMapper problemsMapper;

    private static final String ID = "BB890EB6-3152-4D08-9331-D48FE63198C1";
    private static final String RESOURCE_DIRECTORY = "problem";
    private static final String BASIC_FILE_NAME = "example-1";
    private static final String UK_CORE_CONDITION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Condition";
    private static final String GP_SUMMARY_XPATH = "//pertinentInformation2/pertinentCREType[.//UKCT_MT144038UK02.Problem]";

    @Test
    public void When_MappingFromHl7_Expect_GetId() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        var result = problemsMapper.map(html);

        assertThat(result.get(0).getId()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromHl7_Expect_IdentifierMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        var result = problemsMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getIdentifierFirstRep().getValue())
            .isEqualTo(ID);
    }

    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        problemsMapper.map(html);

        verifyXmlUtilsHits(html, GP_SUMMARY_XPATH);
    }

    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        problemsMapper.map(html);

        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromHl7_Expect_Meta() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        var result = problemsMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_CONDITION_META);
    }

    @Test
    public void When_MappingFromHl7_Expect_DateTimeFormatted() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        var result = problemsMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getOnsetDateTimeType().toHumanDisplay()).isEqualTo("2020-05-01");
    }

    @Test
    public void When_MappingFromHl7_Expect_CategoryMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        var result = problemsMapper.map(html);

        var resultEncounter = (Condition) result.get(0);
        var codingFirstRep = resultEncounter.getCategoryFirstRep().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("162991000000102");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("Problems and Issues");
    }

    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        var result = problemsMapper.map(html);

        var resultEncounter = (Condition) result.get(0);
        var codingFirstRep = resultEncounter.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("181301000000103");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("Abstract problem node");
    }

    @Test
    public void When_MappingFromHl7_Expect_ClinicalStatusMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);

        var result = problemsMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getCode()).isEqualTo("active");

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getSystem())
            .isEqualTo("http://hl7.org/fhir/ValueSet/condition-clinical");

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getDisplay()).isEqualTo("Active");
    }

// Commented out, awaiting further information and action in NIAD-2505
//    @Test
//    public void When_MappingFromHl7_Expect_MatchJson() {
//        var html = getHtmlExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);
//
//        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, BASIC_FILE_NAME);
//
//        var result = problemsMapper.map(html);
//
//        var actualJson = encodeToJson(result.get(0));
//
//        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson.trim());
//    }

}
