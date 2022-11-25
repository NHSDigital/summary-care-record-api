package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test runner for Care Professional Documentation HL7 XML files' conversion to FHIR-JSON.
 *
 * @see: NIAD-2321
 */
@ExtendWith(MockitoExtension.class)
public class CareProfessionalDocumentationsMapperTest extends BaseHL7MapperTest {

    @InjectMocks
    private CareProfessionalDocumentationsMapper mapper;
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";
    private static final String RESOURCE_DIRECTORY = "care-professional-documentation";
    private static final String CATEGORY_DISPLAY = "Care Professional Documentation";
    private static final String FILE_NAME = "example";
    private static final String ID = "7D50E3C0-7565-11E8-AEC7-950876D8FD27";
    private static final String INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144035UK01.CareProfessionalDocumentation]";
    private static final String STATUS_CODE = "COMPLETED";

    /**
     * Load XML file from given path. Test some basic values to be as expected.
     * Tests ID, Meta, Profile and Status.
     */
    @Test
    public void When_MappingFromHL7_Expect_FieldValues() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Communication) result;
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(result.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);
        assertThat(resultObservation.getStatus().toString()).isEqualTo(STATUS_CODE);
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
        var resultObservation = (Communication) result;
        assertThat(resultObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);
    }

    /**
     * Test the status code in the XML is as expected (STATUS_CODE value).
     */
    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var resultObservation = (Communication) result;
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
        var castResult = (Communication) result;
        var codingFirstRep = castResult.getTopic().getCodingFirstRep();

        assertThat(codingFirstRep.getCode()).isEqualTo("308452008");
        assertThat(codingFirstRep.getSystem()).isEqualTo("http://snomed.info/sct");
        assertThat(codingFirstRep.getDisplay()).isEqualTo("Referral to speech and language therapist");
        assertThat(castResult.getCategory().get(0).getCoding().get(0).getDisplay()).isEqualTo(CATEGORY_DISPLAY);
    }

    /**
     * Testing presence and validity of category (<code></code> node).
     */
    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_CategoryMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html);
        var castResult = (Communication) result.get(0);
        var codingFirstRep = castResult.getCategoryFirstRep().getCodingFirstRep();

        assertThat(codingFirstRep.getCode()).isEqualTo("163171000000105");
        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");
        assertThat(codingFirstRep.getDisplay()).isEqualTo("Care Professional Documentation");
    }

    /**
     * Test that the overall JSON expected matches the JSON generated.
     */
    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = mapper.map(html).get(0);
        var actualJson = encodeToJson(result);

        // Assert that the expected JSON matches the created JSON, removing whitespace.
        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson);
    }
}
