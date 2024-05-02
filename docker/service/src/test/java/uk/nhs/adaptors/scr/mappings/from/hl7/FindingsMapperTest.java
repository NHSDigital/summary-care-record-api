package uk.nhs.adaptors.scr.mappings.from.hl7;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FindingsMapperTest extends BaseHL7MapperUtilities {

    @InjectMocks
    private FindingsMapper finding;

    private static final String UK_CORE_OBSERVATION_META = "https://fhir.hl7.org.uk/StructureDefinition/UKCore-Observation";
    private static final String INVESTIGATION_RESOURCE_DIRECTORY = "finding/investigation_results";
    private static final String INVESTIGATION_FILE_NAME = "investigationResults";
    private static final String CLINICAL_RESOURCE_DIRECTORY = "finding/clinical_observations";
    private static final String CLINICAL_OBSERVATIONS_FILE_NAME = "clinicalObservations";
    private static final String GP_SUMMARY_XPATH = "//QUPC_IN210000UK04/ControlActEvent/subject//GPSummary";
    private static final String PERTINENT_CRET_BASE_PATH =
        GP_SUMMARY_XPATH + "/pertinentInformation2/pertinentCREType[.//UKCT_MT144043UK02.Finding]";

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_RandomUUID() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        var result = finding.map(html);

        assertThat(result.get(0).getId()).isEqualTo("19ABC6D1-8AF5-11EA-9FCE-AFDCAECF9DFB");
    }

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        finding.map(html);

        verifyXmlUtilsHits(html, PERTINENT_CRET_BASE_PATH);
    }

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_CodedEntryHit() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        finding.map(html);

        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_DateTimeFormatted() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getEffectivePeriod().getStartElement().toHumanDisplay()).isEqualTo("2020-08-05");
    }

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_MetaUrl() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_OBSERVATION_META);
    }

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_CodingMapped() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);
        var codingFirstRep = resultObservation.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1240601000000108");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("High priority for severe acute respiratory syndrome coronavirus 2 vaccination (finding)");

    }

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_StatusFinal() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getStatus().toString()).isEqualTo("FINAL");

    }

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_CategoryMapped() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        var categoryFirstRep = resultObservation.getCategoryFirstRep().getCodingFirstRep();
        assertThat(categoryFirstRep.getCode()).isEqualTo("163131000000108");

        assertThat(categoryFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(categoryFirstRep.getDisplay()).isEqualTo("Clinical Observations and Findings");

    }

    @Test
    public void When_MappingFromClinicalObservationsHL7_Expect_MatchJson() {
        var html = getHtmlExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);
        var expectedJson = getJsonExample(CLINICAL_RESOURCE_DIRECTORY, CLINICAL_OBSERVATIONS_FILE_NAME);

        var result = finding.map(html);

        var actualJson = encodeToJson(result.get(0));

        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson.trim());
    }

    //InvestigationResults

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_RandomUUID() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        var result = finding.map(html);

        assertThat(result.get(0).getId()).isEqualTo("3CDB62E6-EFEA-4036-9709-52AE7ED7D930");
    }

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_XmlUtilsHit() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        finding.map(html);

        verifyXmlUtilsHits(html, PERTINENT_CRET_BASE_PATH);
    }

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_CodedEntryHit() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        finding.map(html);

        verifyCodedEntryHits();
    }

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_DateTimeFormatted() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getEffectivePeriod().getStartElement().toHumanDisplay()).isEqualTo("2020-08-05");
    }

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_MetaUrl() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getMeta().getProfile().get(0).getValue()).isEqualTo(UK_CORE_OBSERVATION_META);
    }

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_CodingMapped() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);
        var codingFirstRep = resultObservation.getCode().getCodingFirstRep();

        assertThat(codingFirstRep.getCode())
            .isEqualTo("1322781000000102");

        assertThat(codingFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(codingFirstRep.getDisplay())
            .isEqualTo("Severe acute respiratory syndrome coronavirus 2 antigen detection result positive (finding)");

    }

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_StatusFinal() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        assertThat(resultObservation.getStatus().toString()).isEqualTo("FINAL");

    }

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_CategoryMapped() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        var result = finding.map(html);

        var resultObservation = (Observation) result.get(0);

        var categoryFirstRep = resultObservation.getCategoryFirstRep().getCodingFirstRep();
        assertThat(categoryFirstRep.getCode()).isEqualTo("163141000000104");

        assertThat(categoryFirstRep.getSystem())
            .isEqualTo("http://snomed.info/sct");

        assertThat(categoryFirstRep.getDisplay()).isEqualTo("Investigation Results");

    }

    @Test
    public void When_MappingFromInvestigationResultsHL7_Expect_MatchJson() {
        var html = getHtmlExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);
        var expectedJson = getJsonExample(INVESTIGATION_RESOURCE_DIRECTORY, INVESTIGATION_FILE_NAME);

        var result = finding.map(html);

        var actualJson = encodeToJson(result.get(0));

        assertThat(actualJson).isEqualToIgnoringWhitespace(expectedJson.trim());
    }

}
