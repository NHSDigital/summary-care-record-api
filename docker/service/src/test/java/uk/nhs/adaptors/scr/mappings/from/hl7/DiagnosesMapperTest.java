package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.CodedEntryMapper;
import uk.nhs.adaptors.scr.utils.XmlUtils;
import uk.nhs.utils.CareEventMapperArgumentsProvider;
import uk.nhs.utils.DiagnosisMapperArgumentsProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class DiagnosesMapperTest {

    @InjectMocks
    private DiagnosisMapper diagnosisMapper;

    @Mock
    private UuidWrapper uuid;

    @Spy
    private CodedEntryMapper codedEntry = new CodedEntryMapper(new XmlUtils(XPathFactory.newInstance()));

    @Spy
    private XmlUtils xmlUtils = new XmlUtils(XPathFactory.newInstance());

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromHl7_Expect_RandomUUID(String fileName) {
        var html = parseXml(readResourceFile(String.format("diagnosis/%s.html", fileName))).getDocumentElement();

        var result = diagnosisMapper.map(html);

        assertThat(result.get(0).getId()).isEqualTo("bdb64d63-9c06-44fd-b2c1-324c580c6142");
    }

    @SneakyThrows
    private static Document parseXml(String xml) {
        return DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }

}
