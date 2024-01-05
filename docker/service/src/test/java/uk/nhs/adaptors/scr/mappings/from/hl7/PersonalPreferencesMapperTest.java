package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PersonalPreferencesMapperTest  extends BaseHL7MapperTest {

    @InjectMocks
    private PersonalPreferencesMapper personalPreferencesMapper;

    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String RESOURCE_DIRECTORY = "personal_preference";
    private static final String FILE_NAME = "example";
    private static final String ID = "5a3ead29-446d-4ad8-8a2f-aa50a3d026bb";
    private static final String PERTINENT_INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144046UK01.PersonalPreference]";
    private static final String STATUS_CODE = "FINAL";


    @Test
    public void When_MappingFromHl7_Expect_RandomUUID() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = personalPreferencesMapper.map(html).get(0);

        assertThat(result.getId()).isEqualTo("5a3ead29-446d-4ad8-8a2f-aa50a3d026bb");
    }

    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        personalPreferencesMapper.map(html);

        verifyXmlUtilsHits(html, PERTINENT_INFORMATION_BASE_PATH);
    }

    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = personalPreferencesMapper.map(html).get(0);

        var resultObservation = (Observation) result;

        assertThat(resultObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);

    }

    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = personalPreferencesMapper.map(html).get(0);

        var resultObservation = (Observation) result;

        assertThat(resultObservation.getStatus().toString()).isEqualTo(STATUS_CODE);

    }

    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        personalPreferencesMapper.map(html);

        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = personalPreferencesMapper.map(html).get(0);

        var resultObservation = (Observation) result;
        var codingFirstRep = resultObservation.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1240651000000109");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("SARS-CoV-2 (severe acute respiratory syndrome coronavirus 2) vaccination declined");

    }

    @Test
    public void When_MappingFromHl7_Expect_DateTimeFormatted() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = personalPreferencesMapper.map(html).get(0);

        var resultObservation = (Observation) result;

        assertThat(resultObservation.getEffectiveDateTimeType().toHumanDisplay()).isEqualTo("2020-08-05");

    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = personalPreferencesMapper.map(html).get(0);

        var actualJson = encodeToJson(result);

        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson.trim());
    }
}
