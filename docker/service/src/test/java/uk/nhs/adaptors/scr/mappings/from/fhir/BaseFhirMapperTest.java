package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;

import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class BaseFhirMapperTest {

    @Mock
    private UuidWrapper uuid;

    private FhirParser fhirParser = new FhirParser();

    protected static String getExpectedHtml(String resourceDirectory, String fileName) {
        var html = readResourceFile(String.format(resourceDirectory + "/%s.html", fileName));
        return html;
    }

    protected String getJsonExample(String resourceDirectory, String fileName) {
        var jsonFile = readResourceFile(String.format(resourceDirectory + "/%s.json", fileName));
        return jsonFile;
    }

    protected String returnExpectedUuid(String expectedUuid) {
        when(uuid.randomUuid()).thenReturn(expectedUuid);
        return expectedUuid;
    }

    protected <T extends IBaseResource> T getFhirParser(String json, Class<T> classType) {
        var fhirParserResource = fhirParser.parseResource(json, classType);
        return fhirParserResource;
    }

}