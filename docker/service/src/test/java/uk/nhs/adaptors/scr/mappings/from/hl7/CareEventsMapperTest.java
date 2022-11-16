package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Encounter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CareEventsMapperTest extends BaseHL7MapperTest {

    @InjectMocks
    private CareEventsMapper careEvent;

    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Encounter";
    private static final String RESOURCE_DIRECTORY = "care_event";
    private static final String PERTINENT_INFORMATION_BASE_PATH = "/pertinentInformation2/pertinentCREType["
        + ".//UKCT_MT144037UK01.CareEvent]";
    private static final String STATUS_CODE = "FINISHED";
    private static final String ID = "722e35ec-0f00-4b71-b1f9-2240623c6b41";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromHl7_Expect_RandomUUID() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = careEvent.map(html);

        assertThat(result.get(0).getId()).isEqualTo("722e35ec-0f00-4b71-b1f9-2240623c6b41");
    }

    @Test
    public void When_MappingFromHl7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        careEvent.map(html);

        verifyXmlUtilsHits(html, PERTINENT_INFORMATION_BASE_PATH);
    }

    @Test
    public void When_MappingFromHl7_Expect_CodedEntryHit() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        careEvent.map(html);

        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromHl7_Expect_DateTimeFormatted() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);

        assertThat(resultEncounter.getPeriod().getEndElement().toHumanDisplay()).isEqualTo("2020-08-05");

    }

    @Test
    public void When_MappingFromHl7_Expect_MetaUrl() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);

        assertThat(resultEncounter.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_OBSERVATION_META);

    }

    @Test
    public void When_MappingFromHl7_Expect_CodingMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);
        var codignFirstRep = resultEncounter.getTypeFirstRep().getCodingFirstRep();

        assertThat(codignFirstRep.getCode())
            .isEqualTo("1240631000000102");

        assertThat(codignFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codignFirstRep.getDisplay())
            .isEqualTo("Did not attend SARS-CoV-2 (severe acute respiratory syndrome coronavirus 2) vaccination");

    }

    @Test
    public void When_MappingFromHl7_Expect_StatusCode() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);

        assertThat(resultEncounter.getStatus().toString()).isEqualTo(STATUS_CODE);

    }

    @Test
    public void When_MappingFromHl7_Expect_ClassMapped() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);

        assertThat(resultEncounter.getClass_().getCode()).isEqualTo("GENRL");

        assertThat(resultEncounter.getClass_().getSystem())
            .isEqualTo("http://terminology.hl7.org/CodeSystem/v3-ActCode");

        assertThat(resultEncounter.getClass_().getDisplay()).isEqualTo("General");

    }

    @Test
    public void When_MappingFromHl7_Expect_MatchJson() {
        var html = getHtmlExample(RESOURCE_DIRECTORY, FILE_NAME);
        var expectedJson = getJsonExample(RESOURCE_DIRECTORY, FILE_NAME);

        returnExpectedUuid(ID);

        var result = careEvent.map(html);

        var actualJson = encodeToJson(result.get(0));

        assertThat(actualJson).isEqualTo(expectedJson.trim());
    }

}
