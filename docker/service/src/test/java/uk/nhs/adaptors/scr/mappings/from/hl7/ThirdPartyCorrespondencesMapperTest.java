package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;

public class ThirdPartyCorrespondencesMapperTest extends BaseHL7MapperTest {

    private static final String RESOURCE_DIRECTORY = "third_party_correspondence";
    private static final String PERTINENT_INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
            + ".//UKCT_MT144035UK01.ThirdPartyCorrespondence]";
    private static final String STATUS_CODE = "COMPLETED";
    private static final String ID = "7D50E3C0-7565-11E8-AEC7-950876D8FD27";
    private static final String FILE_NAME = "example";

    @InjectMocks
    private ThirdPartyCorrespondencesMapper partyCorrespondencesMapper;

    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";

    @Test
    public void When_MappingFromHl7_Expect_RandomUUID() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = partyCorrespondencesMapper.map(html).get(0);

        assertThat(result.getId()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        partyCorrespondencesMapper.map(html);
        verifyXmlUtilsHits(html, PERTINENT_INFORMATION_BASE_PATH);
    }

    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = partyCorrespondencesMapper.map(html).get(0);
        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);

    }

    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = partyCorrespondencesMapper.map(html).get(0);
        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getStatus().toString()).isEqualTo(STATUS_CODE);

    }

    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        partyCorrespondencesMapper.map(html);
        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = partyCorrespondencesMapper.map(html).get(0);
        var resultCommunication = (Communication) result;
        var codingFirstRep = resultCommunication.getTopic().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
                .isEqualTo("263536004");

        assertThat(codingFirstRep.getSystem())
                .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
                .isEqualTo("Communication");
    }

    /**
     * Testing presence and validity of category (<code></code> node).
     */
    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_CategoryMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = partyCorrespondencesMapper.map(html).get(0);
        var castResult = (Communication) result;
        var codingFirstRep = castResult.getCategoryFirstRep().getCodingFirstRep();

        assertThat(codingFirstRep.getCode()).isEqualTo("163191000000109");
        assertThat(codingFirstRep.getSystem())
                .isEqualTo("http://snomed.info/sct");
        assertThat(codingFirstRep.getDisplay()).isEqualTo("Third Party Correspondence");
    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = partyCorrespondencesMapper.map(html).get(0);
        var actualJson = encodeToJson(result);

        assertThat(actualJson).isEqualTo(expectedJson.trim());
    }
}
