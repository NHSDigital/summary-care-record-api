package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class InvestigationResultsMapperTest extends BaseFhirMapperTest {

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

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var jsonBundle = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Bundle.class);

        var gpSummary = new GpSummary();

        finding.mapObservations(gpSummary, jsonBundle);

        var result = gpSummary.getInvestigationResults();

        assertThat(result.get(0).getCodeCode()).isEqualTo("163131000000108");
        assertThat(result.get(0).getCodeDisplayName())
            .isEqualTo("SARS-CoV-2 (severe acute respiratory syndrome coronavirus 2) RNA (ribonucleic acid) detection result positive");
    }
}
