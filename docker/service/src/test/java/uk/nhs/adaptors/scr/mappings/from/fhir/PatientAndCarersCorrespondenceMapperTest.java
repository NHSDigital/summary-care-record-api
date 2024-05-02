package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.PatientCarerCorrespondence;
import uk.nhs.adaptors.scr.utils.TemplateUtils;


import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PatientAndCarersCorrespondenceMapperTest extends BaseFhirMapperUtilities {

    @InjectMocks
    private PatientAndCarersCorrespondenceMapper patientCarerCorrMapper;

    private static final String ID = "3b3f207f-be82-4ffb-924e-9be0966f5c65";
    private static final String RESOURCE_DIRECTORY = "patient_carer_correspondence";
    private static final String STATUS_CODE = "normal";
    private static final String FILE_NAME = "example";

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);
        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        assertThat(result.getIdRoot()).isEqualTo(ID);
    }

    @Test
    public void When_MappingFromFHIR_Expect_Code() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);


        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        assertThat(result.getCodeCode()).isEqualTo("1240781000000106");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Severe acute respiratory syndrome coronavirus 2 vaccination invitation "
                + "short message service text message sent (situation)");
    }

    @Test
    public void When_MappingFromFHIR_Expect_StatusCode() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        assertThat(result.getStatusCodeCode()).isEqualTo(STATUS_CODE);
    }

    @Test
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow() {
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @Test
    public void When_MappingFromFHIR_Expect_MatchingHtml() {
        var expectedHtml = getExpectedHtml(RESOURCE_DIRECTORY, FILE_NAME);
        var communication = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Communication.class);

        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        var gpSummary = new GpSummary();
        var patientCarerCorr = new ArrayList<PatientCarerCorrespondence>();
        patientCarerCorr.add(result);
        gpSummary.setPatientCarerCorrespondences(patientCarerCorr);

        var patientCarerCorrTemplate = TemplateUtils.loadPartialTemplate("PatientCarerCorrespondences.mustache");

        var resultStr = TemplateUtils.fillTemplate(patientCarerCorrTemplate, gpSummary);
        assertThat(resultStr).isEqualTo(expectedHtml);
    }
}
