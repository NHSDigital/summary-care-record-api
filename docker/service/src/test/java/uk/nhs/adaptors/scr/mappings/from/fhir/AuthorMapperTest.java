package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.utils.AuthorMapperArgumentsProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.utils.Utils.readResourceFile;

class AuthorMapperTest {

    private FhirParser fhirParser = new FhirParser();

    @InjectMocks
    private AuthorMapper authorMapper;

//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(HtmlParserArgumentsProvider.class)
//    public void When_ParsingHtml_Expect_CompositionSectionsAreCreated(String fileName) {
//        var html = parseXml(readResourceFile(String.format("html_parser/%s.html", fileName))).getDocumentElement();
//        var expectedJson = readResourceFile(String.format("html_parser/%s.json", fileName));
//
//        var compositionSections = htmlParser.parse(html);
//
//        var composition = new Composition();
//        composition.setSection(compositionSections);
//
//        var actualJson = fhirParser.encodeToJson(composition);
//
//        assertThat(actualJson).isEqualTo(expectedJson);
//    }

    @ParameterizedTest(name = "[index] - {0}.json")
    @ArgumentsSource(AuthorMapperArgumentsProvider.class)
    public void When_AuthorMap_Expect_authorMapped(String fileName) {
        var expectedJson = readResourceFile(String.format("author_mapper/%s.json", fileName));

        var bundle = fhirParser.parseResource(expectedJson, Bundle.class);
        var gpSummary = new GpSummary();

        authorMapper.mapAuthor(gpSummary, bundle);

        assertThat("1".equals("0"));
    }


    @SneakyThrows
    private static Document parseXml(String xml) {
        return DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }
}
