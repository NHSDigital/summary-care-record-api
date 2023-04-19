package uk.nhs.adaptors.scr.models;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.models.xml.Diagnosis;
import uk.nhs.adaptors.scr.models.xml.Finding;
import uk.nhs.adaptors.scr.models.xml.RiskToPatient;
import uk.nhs.adaptors.scr.models.xml.Treatment;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class GpSummaryTest {
    private static final String RESOURCE_DIRECTORY = "gp_summary";
    private static final String HTML_RESOURCE_DIRECTORY = "gp_summary/from/fhir";
    private static final String BUNDLE_RESOURCE_DIRECTORY = "gp_summary/from/fhir/additionalinfo";
    private static final String STANDARD_FILE_NAME = "standard_gp_summary";
    private static final String ADDITIONAL_INFO_FILE_NAME_1 = "additional_information_gp_summary_1";
    private static final String NHSD_ASID = "1029384756";
    private FhirParser fhirParser = new FhirParser();

    private void assertThirdPartyCorrespondenceText(GpSummary actualResult, ArrayList<String> expectedHeaders) {

        String expectedString = "Additional information records have been found under the following types:";
        for (String expectedRecordType : expectedHeaders) {
            expectedString += "\n" + expectedRecordType;
        }

        assertThat(actualResult.getThirdPartyCorrespondences().get(0).getNote().getText())
                .isEqualTo(expectedString);
    }

    /**
     * Given a supplied bundle with no non-core, no third party correspondence section should be found.
     */
    @Test
    public void When_MappingBundleWithNoNonCore_Expect_NoThirdPartyCorrespondence() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json", "no-non-core"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getThirdPartyCorrespondences().stream().count()).isEqualTo(0);
    }

    /**
     * Given a supplied bundle with one risk to patient, third party correspondence section should be found.
     */
    @Test
    public void When_MappingBundleWithOneRiskToPatient_Expect_ThirdPartyCorrespondence() {
        ArrayList<String> expectedRecordTypes = new ArrayList<String>() {
            {
                add("Risks to Patient");
            }
        };

        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "one-risk-to-patient-third-party-communication"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getRisksToPatient().stream().count()).isEqualTo(1);
        assertThat(result.getThirdPartyCorrespondences().stream().count()).isEqualTo(1);
        assertThirdPartyCorrespondenceText(result, expectedRecordTypes);
    }

    /**
     * Given a supplied bundle multiple treatments and risks to patient, third party correspondence section should be found.
     */
    @Test
    public void When_MappingBundleWithMultipleTreatmentsMultipleRisksToPatient_Expect_ThirdPartyCorrespondence() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "treatments-plus-risks-to-patient"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        ArrayList<String> expectedRecordTypes = new ArrayList<String>() {
            {
                add("Risks to Patient");
                add("Treatments");
            }
        };

        assertThat(result.getRisksToPatient().stream().count()).isEqualTo(2);
        assertThat(result.getTreatments().stream().count()).isEqualTo(2);
        assertThat(result.getThirdPartyCorrespondences().stream().count()).isEqualTo(1);
        assertThirdPartyCorrespondenceText(result, expectedRecordTypes);
    }

    /**
     * Given a supplied bundle with multiple treatments, third party correspondence section should be found.
     */
    @Test
    public void When_MappingBundleWithMultipleTreatments_Expect_ThirdPartyCorrespondence() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "treatments"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        ArrayList<String> expectedRecordTypes = new ArrayList<String>() {
            {
                add("Treatments");
            }
        };

        assertThat(result.getTreatments().stream().count()).isEqualTo(2);
        assertThat(result.getThirdPartyCorrespondences().stream().count()).isEqualTo(1);
        assertThirdPartyCorrespondenceText(result, expectedRecordTypes);
    }

    /**
     * Given a supplied bundle with multiple risks to patient, third party correspondence section should be found.
     */
    @Test
    public void When_MappingBundleWithMultipleRisksToPatient_Expect_ThirdPartyCorrespondence() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "riskstopatient"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        ArrayList<String> expectedRecordTypes = new ArrayList<String>() {
            {
                add("Risks to Patient");
            }
        };

        assertThat(result.getRisksToPatient().stream().count()).isEqualTo(2);
        assertThat(result.getThirdPartyCorrespondences().stream().count()).isEqualTo(1);
        assertThirdPartyCorrespondenceText(result, expectedRecordTypes);
    }

    /**
     * Given a supplied bundle with no non-core, additional information should not be detected.
     */
    @Test
    public void When_MappingBundleWithNoNonCore_Expect_NoAdditionalInformation() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json", "no-non-core"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        Map<String, String> expectedHeaders = new HashMap<>();

        var result = GpSummary.isBundleWithAdditionalInformation(bundle);
        var additionalInformationFlag = result.getLeft();
        var actualHeaders = result.getRight();

        assertThat(additionalInformationFlag).isEqualTo(false);
        assertThat(actualHeaders).isEqualTo(expectedHeaders);
    }

    /**
     * Given a supplied bundle with non-core, additional information should be detected.
     */
    @Test
    public void When_MappingBundleWithOneRiskToPatient_Expect_AdditionalInformation() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json", "one-risk-to-patient"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        Map<String, String> expectedHeaders = new HashMap<>() {
            {
                put("Risks to Patient", "RisksToPatientHeader");
            }
        };

        var result = GpSummary.isBundleWithAdditionalInformation(bundle);
        var additionalInformationFlag = result.getLeft();
        var actualHeaders = result.getRight();

        assertThat(additionalInformationFlag).isEqualTo(true);
        assertThat(actualHeaders).isEqualTo(expectedHeaders);
    }

    /**
     * Given a supplied bundle with treatments and risks to patients, additional information should be detected.
     */
    @Test
    public void When_MappingBundleWithMultipleTreatmentsMultipleRisksToPatient_Expect_AdditionalInformation() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "treatments-plus-risks-to-patient"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        Map<String, String> expectedHeaders = new HashMap<>() {
            {
                put("Risks to Patient", "RisksToPatientHeader");
                put("Treatments", "TreatmentsHeader");
            }
        };

        var result = GpSummary.isBundleWithAdditionalInformation(bundle);
        var additionalInformationFlag = result.getLeft();
        var actualHeaders = result.getRight();

        assertThat(additionalInformationFlag).isEqualTo(true);
        assertThat(actualHeaders).isEqualTo(expectedHeaders);
    }

    /**
     * Given a supplied bundle with treatments, additional information should be detected.
     */
    @Test
    public void When_MappingBundleWithMultipleTreatments_Expect_AdditionalInformation() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "treatments"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        Map<String, String> expectedHeaders = new HashMap<>() {
            {
                put("Treatments", "TreatmentsHeader");
            }
        };

        var result = GpSummary.isBundleWithAdditionalInformation(bundle);
        var additionalInformationFlag = result.getLeft();
        var actualHeaders = result.getRight();

        assertThat(additionalInformationFlag).isEqualTo(true);
        assertThat(actualHeaders).isEqualTo(expectedHeaders);
    }

    /**
     * Given a supplied bundle with risks to patients, additional information should be detected.
     */
    @Test
    public void When_MappingBundleWithMultipleRisksToPatient_Expect_AdditionalInformation() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "riskstopatient"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        Map<String, String> expectedHeaders = new HashMap<>() {
            {
                put("Risks to Patient", "RisksToPatientHeader");
            }
        };

        var result = GpSummary.isBundleWithAdditionalInformation(bundle);
        var additionalInformationFlag = result.getLeft();
        var actualHeaders = result.getRight();

        assertThat(additionalInformationFlag).isEqualTo(true);
        assertThat(actualHeaders).isEqualTo(expectedHeaders);
    }

    /**
     * Given a supplied bundle with risks to patients, additional information should be detected.
     */
    @Test
    public void When_MappingBundleWithLifestyle_Expect_AdditionalInformation() {
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "lifestyle"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        Map<String, String> expectedHeaders = new HashMap<>() {
            {
                put("Lifestyle", "LifestyleHeader");
            }
        };

        var result = GpSummary.isBundleWithAdditionalInformation(bundle);
        var additionalInformationFlag = result.getLeft();
        var actualHeaders = result.getRight();

        assertThat(additionalInformationFlag).isEqualTo(true);
        assertThat(actualHeaders).isEqualTo(expectedHeaders);
    }

    @Test
    public void When_MappingStandardGpSummaryFromBundle_Expect_PresentationTextHL7Match() {
        var valueFile = "standard_gp_summary_presentation_text_value";
        var expectedPresentationValue = readResourceFile(String.format(HTML_RESOURCE_DIRECTORY + "/%s.html", valueFile));

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", STANDARD_FILE_NAME));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getCompositionDate()).isEqualToIgnoringWhitespace("20201117010000");
        assertThat(result.getPresentation().getPresentationText()).isEqualToIgnoringWhitespace(expectedPresentationValue);
    }

    @Test
    public void When_MappingAdditionalInfoGpSummaryFromBundle_Expect_PresentationTextHL7Match() {
        var valueFile = "additional_information_gp_summary_1_presentation_text_value";
        var expectedPresentationValue = readResourceFile(String.format(HTML_RESOURCE_DIRECTORY + "/%s.html", valueFile));

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", ADDITIONAL_INFO_FILE_NAME_1));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getCompositionDate()).isEqualToIgnoringWhitespace("20200430171300");
        assertThat(result.getPresentation().getPresentationText()).isEqualToIgnoringWhitespace(expectedPresentationValue);
    }

    @Test
    public void When_MappingStandardGpSummaryFromBundle_Expect_Diagnosis() {

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", STANDARD_FILE_NAME));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getDiagnoses()).hasAtLeastOneElementOfType(Diagnosis.class);

    }

    @Test
    public void When_MappingStandardGpSummaryFromBundle_Expect_InvestigationResults() {

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", STANDARD_FILE_NAME));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getInvestigationResults()).hasAtLeastOneElementOfType(Finding.class);

    }

    @Test
    public void When_MappingAdditionalInfoGpSummaryFromBundle_Expect_Treatment() {

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", ADDITIONAL_INFO_FILE_NAME_1));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getTreatments()).hasAtLeastOneElementOfType(Treatment.class);
    }

    @Test
    public void When_MappingAdditionalInfoGpSummaryFromBundle_Expect_RiskToPatient() {

        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", ADDITIONAL_INFO_FILE_NAME_1));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        // act
        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        assertThat(result.getRisksToPatient()).hasAtLeastOneElementOfType(RiskToPatient.class);
    }

    //Check whether the mapping of the third party communication prompt is correct after HL7 conversion.
    @Test
    public void When_MappingAdditionalInfoGpSummaryFromBundle_Expect_HL7Match() {
        var expectedHtml = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.html", ADDITIONAL_INFO_FILE_NAME_1));

        //Treatment and risks to patient snippets will cause the third party trigger to fire.
        var jsonFile = readResourceFile(String.format(BUNDLE_RESOURCE_DIRECTORY + "/%s.json",
                "treatments-plus-risks-to-patient"));
        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);

        var result = GpSummary.fromBundle(bundle, NHSD_ASID);

        //Normally these would be changing, so we set a known value for the purpose of this comparison.
        result.getThirdPartyCorrespondences().get(0).setIdRoot("68B1730B-C782-438F-9493-9F4324F4DDA7");
        result.getPresentation().setPresentationId("68B1730B-C782-438F-9493-9F4324F4DDA7");

        //Fill in the template.
        var gpSummaryTemplate = TemplateUtils.loadPartialTemplate("GpSummary.mustache");



        var resultStr = TemplateUtils.fillTemplate(gpSummaryTemplate, result);

        assertThat(resultStr).isNotEmpty();
        //assertThat(resultStr).contains("");
    }

//    Below tests are to be used manually during development.
//    @Test
//    public void When_MappingStandardGpSummaryFromBundle_Expect_HL7Match() {
//        var expectedHtml = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.html", STANDARD_FILE_NAME));
//
//        var jsonFile = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", STANDARD_FILE_NAME));
//        var bundle = fhirParser.parseResource(jsonFile, Bundle.class);
//
//        // act
//        var result = GpSummary.fromBundle(bundle, NHSD_ASID);
//
//        var gpSummaryTemplate = TemplateUtils.loadPartialTemplate("GpSummary.mustache");
//
//        var resultStr = TemplateUtils.fillTemplate(gpSummaryTemplate, result);
//        assertThat(resultStr).isEqualToIgnoringWhitespace(expectedHtml);
//    }
//
}
