package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test runner for Social Or Personal Circumstances HL7 XML files' conversion to FHIR-JSON.
 *
 * @see: NIAD-2324
 */
@ExtendWith(MockitoExtension.class)
public class SocialOrPersonalCircumstancesMapperTest extends BaseHL7MapperTest {

    @InjectMocks
    private SocialOrPersonalCircumstancesMapper mapper;

    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String RESOURCE_DIRECTORY = "social_personal_circumstances";
    private static final String CATEGORY_DISPLAY = "Social or Personal Circumstances";
    private static final String FILE_NAME = "example";
    private static final String ID = "E619AE11-4417-11E9-9B19-B5A20573E36F";
    private static final String INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144036UK01.SocialOrPersonalCircumstance]";
    private static final String STATUS_CODE = "FINAL";

    /**
     * Load XML file from given path. Test some basic values to be as expected.
     * Tests ID, Meta, Profile, Status and Category.
     */
    @Test
    public void When_MappingFromHL7_Expect_FieldValues() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Observation) result;
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);
        assertThat(resultObservation.getStatus().toString()).isEqualTo(STATUS_CODE);
        assertThat(resultObservation.getCategory().get(0).getCoding().get(0).getDisplay()).isEqualTo(CATEGORY_DISPLAY);
    }

    /**
     * Verify presence of XPath of INFORMATION_BASE_PATH.
     */
    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        mapper.map(html);
        verifyXmlUtilsHits(html, INFORMATION_BASE_PATH);
    }

    /**
     * Test the presence/value of the Meta URL.
     */
    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Observation) result;
        assertThat(resultObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);
    }

    /**
     * Test the status code in the XML is as expected (STATUS_CODE value).
     */
    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Observation) result;
        assertThat(resultObservation.getStatus().toString()).isEqualTo(STATUS_CODE);
    }

    /**
     * Test the presence of coded entry xml, i.e. code value, displayName, status and effective time.
     * @see uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper
     */
    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        mapper.map(html);
        verifyCodedEntryHits();
    }

    /**
     * Test the values of coding/code section have mapped correctly.
     */
    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Observation) result;
        var codingFirstRep = resultObservation.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode()).isEqualTo("970531000000100");
        assertThat(codingFirstRep.getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codingFirstRep.getDisplay()).isEqualTo("Main spoken language Cornish");
    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var actualJson = encodeToJson(result);

        assertThat(actualJson).isEqualTo(expectedJson.trim());
    }
}
