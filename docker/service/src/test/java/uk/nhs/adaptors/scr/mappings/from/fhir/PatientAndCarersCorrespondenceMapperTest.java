package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Communication;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.utils.PatientCarerCorrMapperArgumentsProvider;

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

}
