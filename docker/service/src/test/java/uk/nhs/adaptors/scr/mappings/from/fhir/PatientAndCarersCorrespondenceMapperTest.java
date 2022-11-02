package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Procedure;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.PatientCarerCorrespondence;
import uk.nhs.adaptors.scr.models.xml.Treatment;
import uk.nhs.adaptors.scr.utils.TemplateUtils;
import uk.nhs.utils.PatientCarerCorrMapperArgumentsProvider;
import uk.nhs.utils.TreatmentsMapperArgumentsProvider;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class PatientAndCarersCorrespondenceMapperTest {

    @InjectMocks
    private PatientAndCarersCorrespondenceMapper patientCarerCorrMapper;

    @Mock
    private UuidWrapper uuid;

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = readResourceFile(String.format("patient_carer_correspondence/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F582D83-8F89-11EA-8B2D-B741F13EFC47");

        var communication = fhirParser.parseResource(json, Communication.class);

        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        assertThat(result.getIdRoot()).isEqualTo("0F582D83-8F89-11EA-8B2D-B741F13EFC47");

    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_Code(String fileName) {
        var json = readResourceFile(String.format("patient_carer_correspondence/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F582D83-8F89-11EA-8B2D-B741F13EFC47");

        var communication = fhirParser.parseResource(json, Communication.class);

        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        assertThat(result.getCodeCode()).isEqualTo("1240781000000106");
        assertThat(result.getCodeDisplayName())
            .isEqualTo("Severe acute respiratory syndrome coronavirus 2 vaccination invitation short message service text message sent (situation)");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_StatusCode(String fileName) {
        var json = readResourceFile(String.format("patient_carer_correspondence/%s.json", fileName));

        var communication = fhirParser.parseResource(json, Communication.class);

        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        assertThat(result.getStatusCodeCode()).isEqualTo("normal");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_EffectiveTimeLow(String fileName) {
        var json = readResourceFile(String.format("patient_carer_correspondence/%s.json", fileName));

        var communication = fhirParser.parseResource(json, Communication.class);

        var result = patientCarerCorrMapper.mapPatientCarerCorrespondence(communication);

        assertThat(result.getEffectiveTimeLow()).isEqualTo("20200805");
    }

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(PatientCarerCorrMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_MatchingHtml(String fileName) {
        var expectedHtml = readResourceFile(String.format("patient_carer_correspondence/%s.html", fileName));
        var json = readResourceFile(String.format("patient_carer_correspondence/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("0F582D83-8F89-11EA-8B2D-B741F13EFC47");

        var communication = fhirParser.parseResource(json, Communication.class);

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
