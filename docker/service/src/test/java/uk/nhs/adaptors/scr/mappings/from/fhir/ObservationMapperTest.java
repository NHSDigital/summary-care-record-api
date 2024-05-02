package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ObservationMapperTest extends BaseFhirMapperUtilities {

    @InjectMocks
    private ObservationMapper finding;

    private static final String RESOURCE_DIRECTORY = "finding";
    private static final String RESOURCE_DIRECTORY_EXPECTED = "finding/expected";
    private static final String OBSERVATION_BUNDLE_FILE = "observationsBundle";
    private static final String INVESTIGATION_RESULTS_FILE = "investigationResults";
    private static final String CLINICAL_OBS_FILE_NAME = "clinicalObservations";

    @Test
    public void When_MappingFromInvestigationResultsFHIR_Expect_RootId() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getInvestigationResults();

        assertThat(result.get(0).getIdRoot()).isEqualTo("19ABC6D1-8AF5-11EA-9FCE-AFDCAECF9DFB");
    }

    @Test
    public void When_MappingFromInvestigationResultsFHIR_Expect_Code() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getInvestigationResults();

        assertThat(result.get(0).getCodeCode()).isEqualTo("163131000000108");
        assertThat(result.get(0).getCodeDisplayName())
            .isEqualTo("SARS-CoV-2 (severe acute respiratory syndrome coronavirus 2) RNA (ribonucleic acid) detection result positive");
    }

    @Test
    public void When_MappingFromInvestigationResultsFHIR_Expect_StatusCode() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getInvestigationResults();

        assertThat(result.get(0).getStatusCodeCode()).isEqualTo("completed");
    }

    @Test
    public void When_MappingFromInvestigationResultsFHIR_Expect_EffectiveTimeLow() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getInvestigationResults();

        assertThat(result.get(0).getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @Test
    public void When_MappingFromInvestigationResultsFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY_EXPECTED, INVESTIGATION_RESULTS_FILE);
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var findingsTemplate = TemplateUtils.loadPartialTemplate("InvestigationResults.mustache");

        var resultStr = TemplateUtils.fillTemplate(findingsTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }

    //ClinicalObservations
    @Test
    public void When_MappingFromClinicalObservationsFHIR_Expect_RootId() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getClinicalObservationsAndFindings();

        assertThat(result.get(0).getIdRoot()).isEqualTo("19ABC6D1-8AF5-11EA-9FCE-AFDCAECF9DFB");
    }

    @Test
    public void When_MappingFromClinicalObservationsFHIR_Expect_Code() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getClinicalObservationsAndFindings();

        assertThat(result.get(0).getCodeCode()).isEqualTo("1240601000000108");
        assertThat(result.get(0).getCodeDisplayName())
            .isEqualTo("High priority for severe acute respiratory syndrome coronavirus 2 vaccination (finding)");
    }

    @Test
    public void When_MappingFromClinicalObservationsFHIR_Expect_StatusCode() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getClinicalObservationsAndFindings();

        assertThat(result.get(0).getStatusCodeCode()).isEqualTo("completed");
    }

    @Test
    public void When_MappingFromClinicalObservationsFHIR_Expect_EffectiveTimeLow() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getClinicalObservationsAndFindings();

        assertThat(result.get(0).getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @Test
    public void When_MappingFromClinicalObservationsFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY_EXPECTED, CLINICAL_OBS_FILE_NAME);
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, OBSERVATION_BUNDLE_FILE, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var findingsTemplate = TemplateUtils.loadPartialTemplate("ClinicalObservationsAndFindings.mustache");

        var resultStr = TemplateUtils.fillTemplate(findingsTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }


}
