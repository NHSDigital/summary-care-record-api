package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;
import uk.nhs.utils.CareEventMapperArgumentsProvider;
import uk.nhs.adaptors.scr.components.FhirParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class CareEventsMapperTest {

    @InjectMocks
    private CareEventsMapper careEvent;

    @Mock
    private UuidWrapper uuid;

    @Spy
    private CodedEntryMapper codedEntry = new CodedEntryMapper(new XmlUtils(XPathFactory.newInstance()));

    @Spy
    private XmlUtils xmlUtils = new XmlUtils(XPathFactory.newInstance());

    private FhirParser fhirParser = new FhirParser();
    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Encounter";
// getNodeListByXPath
    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_RandomUUID(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

        var result = careEvent.map(html);

        assertThat(result.get(0).getId()).isEqualTo("722e35ec-0f00-4b71-b1f9-2240623c6b41");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_XmlUtilsHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

        careEvent.map(html);

        verify(xmlUtils, times(1))
            .getNodeListByXPath(html, "/pertinentInformation2/pertinentCREType[.//UKCT_MT144037UK01.CareEvent]");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodedEntryHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

        careEvent.map(html);

        verify(codedEntry, times(1)).getCommonCodedEntryValues(any(Element.class));
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_DateTimeFormatted(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);

        assertThat(resultEncounter.getPeriod().getEndElement().toHumanDisplay()).isEqualTo("2020-08-05");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MetaUrl(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);

        assertThat(resultEncounter.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_OBSERVATION_META);

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodingMapped(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

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

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_StatusFinished(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);

        assertThat(resultEncounter.getStatus().toString()).isEqualTo("FINISHED");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_ClassMapped(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

        var result = careEvent.map(html);

        var resultEncounter = (Encounter) result.get(0);

        assertThat(resultEncounter.getClass_().getCode()).isEqualTo("GENRL");

        assertThat(resultEncounter.getClass_().getSystem())
            .isEqualTo("http://terminology.hl7.org/CodeSystem/v3-ActCode");

        assertThat(resultEncounter.getClass_().getDisplay()).isEqualTo("General");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MatchJson(String fileName) {
        var html = parseXml(readResourceFile(String.format("care_event/%s.html", fileName))).getDocumentElement();
        var expectedJson = readResourceFile(String.format("care_event/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("722e35ec-0f00-4b71-b1f9-2240623c6b41");

        var result = careEvent.map(html);

        var actualJson = fhirParser.encodeToJson(result.get(0));

        assertThat(actualJson).isEqualTo(expectedJson.trim());
    }

    @SneakyThrows
    private static Document parseXml(String xml) {
        return DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }

}
