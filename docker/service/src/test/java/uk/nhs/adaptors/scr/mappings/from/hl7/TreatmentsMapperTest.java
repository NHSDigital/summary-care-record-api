package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class TreatmentsMapperTest extends BaseHL7MapperTest {

    @InjectMocks
    private TreatmentsMapper treatmentsMapper;

    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Procedure";
    private static final String RESOURCE_DIRECTORY = "treatments";
    private static final String PERTINENT_INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144055UK01.Treatment]";
    private static final String STATUS_CODE = "COMPLETED";
    private static final String ID = "870bfee5-67e6-4611-8b62-e4609e2f3105";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromHl7_Expect_RandomUUID() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = treatmentsMapper.map(html).get(0);

        assertThat(result.getId()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        treatmentsMapper.map(html);

        verifyXmlUtilsHits(html, PERTINENT_INFORMATION_BASE_PATH);
    }

    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = treatmentsMapper.map(html).get(0);

        var resultProcedure = (Procedure) result;

        assertThat(resultProcedure.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);

    }

    @Test
    public void When_MappingFromHl7_Expect_StatusCompleted() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = treatmentsMapper.map(html).get(0);

        var resultProcedure = (Procedure) result;

        assertThat(resultProcedure.getStatus().toString()).isEqualTo(STATUS_CODE);

    }

    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        treatmentsMapper.map(html);

        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = treatmentsMapper.map(html).get(0);

        var resultProcedure = (Procedure) result;
        var codingFirstRep = resultProcedure.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1240491000000103");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("Severe acute respiratory syndrome coronavirus 2 vaccination (procedure)");

    }

    @Test
    public void When_MappingFromHl7_Expect_DateTimeFormatted() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = treatmentsMapper.map(html).get(0);

        var resultProcedure = (Procedure) result;

        assertThat(resultProcedure.getPerformedDateTimeType().toHumanDisplay()).isEqualTo("2020-08-05");

    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = treatmentsMapper.map(html).get(0);

        var actualJson = encodeToJson(result);

        assertThat(actualJson).isEqualTo(expectedJson.trim());
    }
}
