package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.CareEvent;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import uk.nhs.utils.CareEventMapperArgumentsProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class CareEventMapperTest {

    @InjectMocks
    private CareEventMapper careEvent;

    @Mock
    private UuidWrapper uuid;

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = readResourceFile(String.format("care_event/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F582D91-8F89-11EA-8B2D-B741F13EFC47");

        var encounter = fhirParser.parseResource(json, Encounter.class);

        var result = careEvent.mapCareEvent(encounter);

        assertThat(result.getIdRoot()).isEqualTo("0F582D91-8F89-11EA-8B2D-B741F13EFC47");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_Code(String fileName) {
        var json = readResourceFile(String.format("care_event/%s.json", fileName));

        var encounter = fhirParser.parseResource(json, Encounter.class);

        var result = careEvent.mapCareEvent(encounter);

        assertThat(result.getCodeCode()).isEqualTo("1240631000000102");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Did not attend SARS-CoV-2 (severe acute respiratory syndrome coronavirus 2) vaccination");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
        var json = readResourceFile(String.format("care_event/%s.json", fileName));

        var encounter = fhirParser.parseResource(json, Encounter.class);

        var result = careEvent.mapCareEvent(encounter);

        assertThat(result.getStatusCodeCode()).isEqualTo("normal");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
        var json = readResourceFile(String.format("care_event/%s.json", fileName));

        var encounter = fhirParser.parseResource(json, Encounter.class);

        var result = careEvent.mapCareEvent(encounter);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
        var expectedHtml = readResourceFile(String.format("care_event/%s.html", fileName));
        var json = readResourceFile(String.format("care_event/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F582D91-8F89-11EA-8B2D-B741F13EFC47");

        var encounter = fhirParser.parseResource(json, Encounter.class);

        var result = careEvent.mapCareEvent(encounter);

        var gpSummary = new GpSummary();
        var careEvents = new ArrayList<CareEvent>();
        careEvents.add(result);
        gpSummary.setCareEvents(careEvents);

        var careEventsTemplate = TemplateUtils.loadPartialTemplate("CareEvents.mustache");

        var resultStr = TemplateUtils.fillTemplate(careEventsTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }

    @SneakyThrows
    private static Document parseXml(String xml) {
        return DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }

}
