package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PatientAndCarerCorrespondenceMapperTest extends BaseHL7MapperTest {

    @InjectMocks
    private PatientAndCarerCorrespondenceMapper patientCarerCorrMapper;


    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";
    private static final String RESOURCE_DIRECTORY = "patient_carer_correspondence";
    private static final String PERTINENT_INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144035UK01.PatientCarerCorrespondence]";
    private static final String STATUS_CODE = "COMPLETED";
    private static final String ID = "3b3f207f-be82-4ffb-924e-9be0966f5c65";
    private static final String FILE_NAME = "example";


    @Test
    public void When_MappingFromHl7_Expect_RandomUUID() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = patientCarerCorrMapper.map(html).get(0);

        assertThat(result.getId()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        patientCarerCorrMapper.map(html);
        verifyXmlUtilsHits(html, PERTINENT_INFORMATION_BASE_PATH);
    }

    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = patientCarerCorrMapper.map(html).get(0);
        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);

    }

    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = patientCarerCorrMapper.map(html).get(0);
        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getStatus().toString()).isEqualTo(STATUS_CODE);

    }

    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        patientCarerCorrMapper.map(html);
        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = patientCarerCorrMapper.map(html).get(0);
        var resultCommunication = (Communication) result;
        var codingFirstRep = resultCommunication.getTopic().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1240781000000106");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("Severe acute respiratory syndrome coronavirus 2 vaccination invitation "
                + "short message service text message sent (situation)");
    }

    /**
     * Testing presence and validity of category (<code></code> node).
     */
    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_CategoryMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = patientCarerCorrMapper.map(html).get(0);
        var castResult = (Communication) result;
        var codingFirstRep = castResult.getCategoryFirstRep().getCodingFirstRep();

        assertThat(codingFirstRep.getCode()).isEqualTo("163181000000107");
        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");
        assertThat(codingFirstRep.getDisplay()).isEqualTo("Patient/carer correspondence");
    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = patientCarerCorrMapper.map(html).get(0);
        var actualJson = encodeToJson(result);

        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson.trim());
    }
}
