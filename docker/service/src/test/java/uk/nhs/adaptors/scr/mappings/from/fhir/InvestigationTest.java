package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.utils.InvestigationMapperArgumentsProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class InvestigationTest {

    @InjectMocks
    private InvestigationMapper investigation;

    @Mock
    private UuidWrapper uuid;

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(InvestigationMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = readResourceFile(String.format("investigation/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F5A9E75-8F89-11EA-8B2D-B741F13EFC47");

        var procedure = fhirParser.parseResource(json, Procedure.class);

        var result = investigation.mapInvestigation(procedure);

        assertThat(result.getIdRoot()).isEqualTo("0F5A9E75-8F89-11EA-8B2D-B741F13EFC47");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(InvestigationMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_Code(String fileName) {
        var json = readResourceFile(String.format("investigation/%s.json", fileName));

        var procedure = fhirParser.parseResource(json, Procedure.class);

        var result = investigation.mapInvestigation(procedure);

        assertThat(result.getCodeCode()).isEqualTo("1240461000000109");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Measurement of severe acute respiratory syndrome coronavirus 2 antibody (procedure)");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(InvestigationMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
        var json = readResourceFile(String.format("investigation/%s.json", fileName));

        var procedure = fhirParser.parseResource(json, Procedure.class);

        var result = investigation.mapInvestigation(procedure);

        assertThat(result.getStatusCodeCode()).isEqualTo("normal");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(InvestigationMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
        var json = readResourceFile(String.format("investigation/%s.json", fileName));

        var procedure = fhirParser.parseResource(json, Procedure.class);

        var result = investigation.mapInvestigation(procedure);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }
//
//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(CareEventMapperArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
//        var expectedHtml = readResourceFile(String.format("investigation/%s.html", fileName));
//        var json = readResourceFile(String.format("investigation/%s.json", fileName));
//
//        when(uuid.randomUuid()).thenReturn("0F582D91-8F89-11EA-8B2D-B741F13EFC47");
//
//        var encounter = fhirParser.parseResource(json, Encounter.class);
//
//        var result = careEvent.mapCareEvent(encounter);
//
//        var gpSummary = new GpSummary();
//        var careEvents = new ArrayList<CareEvent>();
//        careEvents.add(result);
//        gpSummary.setCareEvents(careEvents);
//
//        var careEventsTemplate = TemplateUtils.loadPartialTemplate("CareEvents.mustache");
//
//        var resultStr = TemplateUtils.fillTemplate(careEventsTemplate, gpSummary);
//        assertThat(resultStr).isEqualTo(expectedHtml);
//    }
}
