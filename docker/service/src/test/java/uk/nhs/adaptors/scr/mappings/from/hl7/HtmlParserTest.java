package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Composition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.controllers.FhirMediaTypes;
import uk.nhs.utils.HtmlParserArgumentsProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.utils.Utils.readResourceFile;

class HtmlParserTest {

    private FhirParser fhirParser = new FhirParser();
    private HtmlParser htmlParser = new HtmlParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(HtmlParserArgumentsProvider.class)
    public void When_ParsingHtml_Expect_CompositionSectionsAreCreated(String fileName) throws HttpMediaTypeNotAcceptableException {
        var html = parseXml(readResourceFile(String.format("html_parser/%s.html", fileName))).getDocumentElement();
        var expectedJson = readResourceFile(String.format("html_parser/%s.json", fileName));

        var compositionSections = htmlParser.parse(html);

        var composition = new Composition();
        composition.setSection(compositionSections);

        var actualJson = fhirParser
            .encodeResource(FhirMediaTypes.APPLICATION_FHIR_JSON, composition);

        assertThat(actualJson).isEqualTo(expectedJson);
    }

    @SneakyThrows
    private static Document parseXml(String xml) {
        return DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }
}
