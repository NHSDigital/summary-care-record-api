package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FamilyHistoryMapperTest extends BaseFhirMapperTest {

    private static final String RESOURCE_DIRECTORY = "family_history";
    private static final String STATUS_CODE = "normal";
    private static final String FILE_NAME = "without_author";
    private static final String FILE_NAME_HL7 = "without_author";

    @InjectMocks
    private FamilyHistoryMapper familyHistoryMapper;

    @Test
    public void When_MappingFromFHIR_Expect_RootId() {
        var obj = getFileAsObject(RESOURCE_DIRECTORY, FILE_NAME, Observation.class);
        var result = familyHistoryMapper.map(obj);
        assertThat(result.getIdRoot()).isEqualTo("51089E5B-0840-4237-8D91-CFC0238E83B4");
    }
}
