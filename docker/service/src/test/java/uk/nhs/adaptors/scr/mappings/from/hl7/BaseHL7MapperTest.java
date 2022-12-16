package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.extension.ExtendWith;
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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class BaseHL7MapperTest {

    @Mock
    private UuidWrapper uuid;

    @Spy
    private CodedEntryMapper codedEntry = new CodedEntryMapper(new XmlUtils(XPathFactory.newInstance()));

    @Spy
    private XmlUtils xmlUtils = new XmlUtils(XPathFactory.newInstance());

    private FhirParser fhirParser = new FhirParser();

    @SneakyThrows
    protected static Document parseXml(String xml) {
        return DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }

    protected String returnExpectedUuid(String expectedUuid) {
        when(uuid.randomUuid()).thenReturn(expectedUuid);
        return expectedUuid;
    }

    protected static Element getHtmlExample(String resourceDirectory, String fileName) {
        var htmlFile = parseXml(readResourceFile(String.format(resourceDirectory + "/%s.html", fileName)))
            .getDocumentElement();
        return htmlFile;
    }

    protected String getJsonExample(String resourceDirectory, String fileName) {
        var jsonFile = readResourceFile(String.format(resourceDirectory + "/%s.json", fileName));
        return jsonFile;
    }

    protected <T extends IBaseResource> T getFileAsObject(String resourceDirectory, String fileName, Class<T> classType) {
        var jsonFile = readResourceFile(String.format(resourceDirectory + "/%s.json", fileName));
        var fhirParserResource = fhirParser.parseResource(jsonFile, classType);
        return fhirParserResource;
    }

    protected void verifyCodedEntryHits() {
        verify(codedEntry, times(1)).getCommonCodedEntryValues(any(Element.class));
    }

    protected void verifyXmlUtilsHits(Element html, String pertinentBasePath) {
        verify(xmlUtils, times(1)).getNodeListByXPath(html, pertinentBasePath);
    }

    protected String encodeToJson(Resource result) {
        var actualJson = fhirParser.encodeToJson(result);
        return actualJson;
    }

}
