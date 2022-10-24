package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Condition;
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
import uk.nhs.utils.DiagnosisMapperArgumentsProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class DiagnosisMapperTest {

    @InjectMocks
    private DiagnosisMapper diagnosisMapper;

    @Mock
    private UuidWrapper uuid;

    @Spy
    private CodedEntryMapper codedEntry = new CodedEntryMapper(new XmlUtils(XPathFactory.newInstance()));

    @Spy
    private XmlUtils xmlUtils = new XmlUtils(XPathFactory.newInstance());

    private FhirParser fhirParser = new FhirParser();

    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//"
        + "GPSummary/pertinentInformation2/pertinentCREType[.//UKCT_MT144042UK01.Diagnosis]";

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_GetId(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();

        var result = diagnosisMapper.map(html);

        assertThat(result.get(0).getId()).isEqualTo("AF0AAF00-797C-11EA-B378-F1A7EC384595");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_XmlUtilsHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();

        diagnosisMapper.map(html);

        verify(xmlUtils, times(1))
            .getNodeListByXPath(html, GP_SUMMARY_XPATH);
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodedEntryHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();

        diagnosisMapper.map(html);

        verify(codedEntry, times(1)).getCommonCodedEntryValues(any(Element.class));
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_DateTimeFormatted(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();

        var result = diagnosisMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getOnsetDateTimeType().toHumanDisplay()).isEqualTo("2020-08-05");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MetaUrl(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();

        var result = diagnosisMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_OBSERVATION_META);
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodingMapped(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();

        var result = diagnosisMapper.map(html);

        var resultEncounter = (Condition) result.get(0);
        var codingFirstRep = resultEncounter.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1300721000000109");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("COVID-19 confirmed by laboratory test");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_ClinicalStatusMapped(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();

        var result = diagnosisMapper.map(html);

        var resultCondition = (Condition) result.get(0);

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getCode()).isEqualTo("active");

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getSystem())
            .isEqualTo("http://hl7.org/fhir/ValueSet/condition-clinical");
        // this is not working due to it not being mapped + url is different in map class

        assertThat(resultCondition.getClinicalStatus().getCodingFirstRep().getDisplay()).isEqualTo("Active");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MatchJson(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();
        var expectedJson = readResourceFile(String.format("diagnosis/%s.json", fileName));

        var result = diagnosisMapper.map(html);

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
