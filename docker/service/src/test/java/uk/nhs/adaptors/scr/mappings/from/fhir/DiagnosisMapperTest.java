package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.utils.DiagnosisMapperArgumentsProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class DiagnosisMapperTest {

    @InjectMocks
    private DiagnosisMapper diagnosisMapper;

    @Mock
    private UuidWrapper uuid;

    private FhirParser fhirParser = new FhirParser();

    @ParameterizedTest(name = "[{index}] - {0}.html/json")
    @ArgumentsSource(DiagnosisMapperArgumentsProvider.class)
    public void When_MappingFromFHIR_Expect_RootId(String fileName) {
        var json = readResourceFile(String.format("diagnosis/%s.json", fileName));

        when(uuid.randomUuid()).thenReturn("AF0AAF00-797C-11EA-B378-F1A7EC384595");

        var condition = fhirParser.parseResource(json, Condition.class);

        var result = diagnosisMapper.mapDiagnosis(condition);

        assertThat(result.getIdRoot()).isEqualTo("AF0AAF00-797C-11EA-B378-F1A7EC384595");
    }


}
