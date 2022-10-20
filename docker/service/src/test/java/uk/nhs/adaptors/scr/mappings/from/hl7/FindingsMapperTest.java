package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;
import uk.nhs.utils.FindingMapperArgumentsProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class FindingsMapperTest {

    @InjectMocks
    private FindingsMapper finding;

    @Spy
    private CodedEntryMapper codedEntry = new CodedEntryMapper(new XmlUtils(XPathFactory.newInstance()));

    @Spy
    private XmlUtils xmlUtils = new XmlUtils(XPathFactory.newInstance());

    private FhirParser fhirParser = new FhirParser();
    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_RandomUUID(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();

        var result = finding.map(html);

        assertThat(result.get(0).getId()).isEqualTo("19ABC6D1-8AF5-11EA-9FCE-AFDCAECF9DFB");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_XmlUtilsHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();

        finding.map(html);

        verify(xmlUtils, times(1))
            .getNodeListByXPath(html, "/pertinentInformation2/pertinentCREType[.//UKCT_MT144043UK02.Finding]");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodedEntryHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();

        finding.map(html);

        verify(codedEntry, times(1)).getCommonCodedEntryValues(any(Element.class));
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_DateTimeFormatted(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getEffectivePeriod().getStartElement().toHumanDisplay()).isEqualTo("2020-08-05");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MetaUrl(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_OBSERVATION_META);
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodingMapped(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);
        var codingFirstRep = resultObservation.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1240601000000108");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://terminology.hl7.org/CodeSystem/v3-ActCode");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("High priority for severe acute respiratory syndrome coronavirus 2 vaccination (finding)");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_StatusFinal(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getStatus().toString()).isEqualTo("FINAL");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CategoryMapped(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        var categoryFirstRep = resultObservation.getCategoryFirstRep().getCodingFirstRep();
        assertThat(categoryFirstRep.getCode()).isEqualTo("163131000000108");

        assertThat(categoryFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(categoryFirstRep.getDisplay()).isEqualTo("Clinical Observations and Findings");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(FindingMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MatchJson(String fileName) {
        var html = parseXml(readResourceFile(String.format("finding/%s.html", fileName))).getDocumentElement();
        var expectedJson = readResourceFile(String.format("finding/%s.json", fileName));

        var result = finding.map(html);

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
