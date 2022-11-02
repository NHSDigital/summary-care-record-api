package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Communication;
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
import uk.nhs.utils.PatientCarerCorrMapperArgumentsProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class PatientAndCarerCorrespondenceMapperTest {

    @InjectMocks
    private PatientAndCarerCorrespondenceMapper patientCarerCorrMapper;

    @Mock
    private UuidWrapper uuid;

    @Spy
    private CodedEntryMapper codedEntry = new CodedEntryMapper(new XmlUtils(XPathFactory.newInstance()));

    @Spy
    private XmlUtils xmlUtils = new XmlUtils(XPathFactory.newInstance());

    private FhirParser fhirParser = new FhirParser();
    private static final String UK_CORE_PROCEDURE_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Communication";

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_RandomUUID(String fileName) {
        var html = parseXml(readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("3b3f207f-be82-4ffb-924e-9be0966f5c65");

        var result = patientCarerCorrMapper.map(html).get(0);

        assertThat(result.getId()).isEqualTo("3b3f207f-be82-4ffb-924e-9be0966f5c65");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_XmlUtilsHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("3b3f207f-be82-4ffb-924e-9be0966f5c65");

        patientCarerCorrMapper.map(html);

        verify(xmlUtils, times(1))
            .getNodeListByXPath(html, "/pertinentInformation2/pertinentCREType[.//UKCT_MT144035UK01.PatientCarerCorrespondence]");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MetaUrl(String fileName) {
        var html = parseXml(readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("3b3f207f-be82-4ffb-924e-9be0966f5c65");

        var result = patientCarerCorrMapper.map(html).get(0);

        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_PROCEDURE_META);

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_StatusCompleted(String fileName) {
        var html = parseXml(readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("3b3f207f-be82-4ffb-924e-9be0966f5c65");

        var result = patientCarerCorrMapper.map(html).get(0);

        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getStatus().toString()).isEqualTo("COMPLETED");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodedEntryHit(String fileName) {
        var html = parseXml(readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("3b3f207f-be82-4ffb-924e-9be0966f5c65");

        patientCarerCorrMapper.map(html);

        verify(codedEntry, times(1)).getCommonCodedEntryValues(any(Element.class));
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_CodingMapped(String fileName) {
        var html = parseXml(readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("3b3f207f-be82-4ffb-924e-9be0966f5c65");

        var result = patientCarerCorrMapper.map(html).get(0);

        var resultCommunication = (Communication) result;
        var codingFirstRep = resultCommunication.getTopic().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1240781000000106");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("Severe acute respiratory syndrome coronavirus 2 vaccination invitation short message service text message sent (situation)");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_DateTimeFormatted(String fileName) {
        var html = parseXml(readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName))).getDocumentElement();

        when(uuid.randomUuid()).thenReturn("3b3f207f-be82-4ffb-924e-9be0966f5c65");

        var result = patientCarerCorrMapper.map(html).get(0);

        var resultCommunication = (Communication) result;

        assertThat(resultCommunication.getSentElement().toHumanDisplay()).isEqualTo("5 Aug 2020, 00:00:00");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_MatchJson(String fileName) {
        var html = parseXml(readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName))).getDocumentElement();
        var expectedJson = readResourceFile(String.format("patient_carer_correspondence/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("3b3f207f-be82-4ffb-924e-9be0966f5c65");

        var result = patientCarerCorrMapper.map(html).get(0);

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
