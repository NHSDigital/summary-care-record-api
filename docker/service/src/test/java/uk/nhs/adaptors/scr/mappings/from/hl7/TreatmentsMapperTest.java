package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Procedure;
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
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;
import uk.nhs.utils.InvestigationMapperArgumentsProvider;
import uk.nhs.utils.TreatmentsMapperArgumentsProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class TreatmentsMapperTest {

    @InjectMocks
    private TreatmentsMapper treatmentsMapper;

    @Mock
    private UuidWrapper uuid;

    @Spy
    private CodedEntryMapper codedEntry = new CodedEntryMapper(new XmlUtils(XPathFactory.newInstance()));

    @Spy
    private XmlUtils xmlUtils = new XmlUtils(XPathFactory.newInstance());

    private FhirParser fhirParser = new FhirParser();
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Procedure";

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(TreatmentsMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_RandomUUID(String fileName) {
        var html = parseXml(readResourceFile(String.format("treatments/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("870bfee5-67e6-4611-8b62-e4609e2f3105");

        var result = treatmentsMapper.map(html).get(0);

        assertThat(result.getId()).isEqualTo("870bfee5-67e6-4611-8b62-e4609e2f3105");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(TreatmentsMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_XmlUtilsHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("treatments/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("870bfee5-67e6-4611-8b62-e4609e2f3105");

        var result = treatmentsMapper.map(html).get(0);

        verify(xmlUtils, times(1))
            .getNodeListByXPath(html, "/pertinentInformation2/pertinentCREType[.//UKCT_MT144055UK01.Treatment]");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(TreatmentsMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MetaUrl(String fileName) {
        var html = parseXml(readResourceFile(String.format("treatments/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("870bfee5-67e6-4611-8b62-e4609e2f3105");

        var result = treatmentsMapper.map(html).get(0);

        var resultProcedure = (Procedure) result;

        assertThat(resultProcedure.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(TreatmentsMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_StatusCompleted(String fileName) {
        var html = parseXml(readResourceFile(String.format("treatments/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("870bfee5-67e6-4611-8b62-e4609e2f3105");

        var result = treatmentsMapper.map(html).get(0);

        var resultProcedure = (Procedure) result;

        assertThat(resultProcedure.getStatus().toString()).isEqualTo("COMPLETED");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(TreatmentsMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodedEntryHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("treatments/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("870bfee5-67e6-4611-8b62-e4609e2f3105");

        var result = treatmentsMapper.map(html).get(0);

        verify(codedEntry, times(1)).getCommonCodedEntryValues(any(Element.class));
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(TreatmentsMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodingMapped(String fileName) {
        var html = parseXml(readResourceFile(String.format("treatments/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("870bfee5-67e6-4611-8b62-e4609e2f3105");

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

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(TreatmentsMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_DateTimeFormatted(String fileName) {
        var html = parseXml(readResourceFile(String.format("treatments/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("870bfee5-67e6-4611-8b62-e4609e2f3105");

        var result = treatmentsMapper.map(html).get(0);

        var resultProcedure = (Procedure) result;

        assertThat(resultProcedure.getPerformedDateTimeType().toHumanDisplay()).isEqualTo("2020-08-05");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(TreatmentsMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MatchJson(String fileName) {
        var html = parseXml(readResourceFile(String.format("treatments/%s.html", fileName))).getDocumentElement();
        var expectedJson = readResourceFile(String.format("treatments/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("870bfee5-67e6-4611-8b62-e4609e2f3105");

        var result = treatmentsMapper.map(html).get(0);

        var actualJson = fhirParser.encodeToJson(result);

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