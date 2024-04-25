package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProvisionsOfAdviceAndInfoMapperTest extends BaseHL7MapperUtilities {

    @InjectMocks
    private ProvisionsOfAdviceAndInfoMapper provisionsOfAdviceAndInfoMapper;

    private static final String RESOURCE_DIRECTORY = "provision_of_advice_info";
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";
    private static final String PERTINENT_INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144049UK01.ProvisionOfAdviceAndInformation]";
    private static final String STATUS_CODE = "COMPLETED";
    private static final String ID = "0F582D83-8F89-11EA-8B2D-B741F13EFC47";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromHl7_Expect_CorrectUUID() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = provisionsOfAdviceAndInfoMapper.map(html).get(0);

        assertThat(result.getId()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        provisionsOfAdviceAndInfoMapper.map(html);
        verifyXmlUtilsHits(html, PERTINENT_INFORMATION_BASE_PATH);
    }

    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = provisionsOfAdviceAndInfoMapper.map(html).get(0);
        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);

    }

    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = provisionsOfAdviceAndInfoMapper.map(html).get(0);
        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getStatus().toString()).isEqualTo(STATUS_CODE);

    }

    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        provisionsOfAdviceAndInfoMapper.map(html);
        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = provisionsOfAdviceAndInfoMapper.map(html).get(0);
        var resultCommunication = (Communication) result;
        var codingFirstRep = resultCommunication.getTopic().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1240711000000104");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("Educated about severe acute respiratory syndrome coronavirus 2 infection (situation)");

    }

    @Test
    public void When_MappingFromHl7_Expect_DateTimeFormatted() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = provisionsOfAdviceAndInfoMapper.map(html).get(0);
        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getSentElement().toHumanDisplay()).isEqualTo("2020-08-05");

    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);
        var result = provisionsOfAdviceAndInfoMapper.map(html).get(0);
        var actualJson = encodeToJson(result);

        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson.trim());
    }

}
