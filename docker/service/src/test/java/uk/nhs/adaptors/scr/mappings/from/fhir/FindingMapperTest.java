package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;


import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FindingMapperTest extends BaseFhirMapperTest {

    @InjectMocks
    private ObservationMapper finding;

    private static final String RESOURCE_DIRECTORY = "finding";
    private static final String FILE_NAME = "gpSummaryExample";

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getInvestigationResults();

        assertThat(result.get(0).getIdRoot()).isEqualTo("19ABC6D1-8AF5-11EA-9FCE-AFDCAECF9DFB");
    }
//
//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(FindingMapperArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_Code(String fileName) {
//        var json = readResourceFile(String.format("finding/%s.json", fileName));
//
//        var observation = fhirParser.parseResource(json, Observation.class);
//
//        var result = finding.mapFinding(observation);
//
//        assertThat(result.getCodeCode()).isEqualTo("1240601000000108");
//        assertThat(result.getCodeDisplayName())
//            .isEqualTo("High priority for severe acute respiratory syndrome coronavirus 2 vaccination (finding)");
//    }
//
//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(FindingMapperArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
//        var json = readResourceFile(String.format("finding/%s.json", fileName));
//
//        var observation = fhirParser.parseResource(json, Observation.class);
//
//        var result = finding.mapFinding(observation);
//
//        assertThat(result.getStatusCodeCode()).isEqualTo("completed");
//    }
//
//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(FindingMapperArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
//        var json = readResourceFile(String.format("finding/%s.json", fileName));
//
//        var observation = fhirParser.parseResource(json, Observation.class);
//
//        var result = finding.mapFinding(observation);
//
//        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
//    }
//
//    @ParameterizedTest(name = "[{index}] - {0}.html/json")
//    @ArgumentsSource(FindingMapperArgumentsProvider.class)
//    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
//        var expectedHtml = readResourceFile("finding/expected/expected.html");
//        var json = readResourceFile(String.format("finding/%s.json", fileName));
//
//        var observation = fhirParser.parseResource(json, Observation.class);
//
//        var result = finding.mapFinding(observation);
//
//        var gpSummary = new GpSummary();
//        var findings = new ArrayList<Finding>();
//        findings.add(result);
//        gpSummary.setClinicalObservationsAndFindings(findings);
//
//        var findingsTemplate = TemplateUtils.loadPartialTemplate("ClinicalObservationsAndFindings.mustache");
//
//        var resultStr = TemplateUtils.fillTemplate(findingsTemplate, gpSummary);
//        assertThat(resultStr).isEqualTo(expectedHtml);
//    }

}
