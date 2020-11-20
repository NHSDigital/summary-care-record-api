package uk.nhs.adaptors.scr.uat.common;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Builder
@Getter
public class TestData {
    public static final String FHIR_JSON_FILE_ENDING = ".fhir.json";
    public static final String HL7V3_FILE_ENDING = ".hl7v3.xml";

    private final String fhir;
    private final String hl7v3;
    private final FhirFormat fhirFormat;

    public static TestData build(List<Resource> resources) {
        if (resources.size() != 2) {
            throw new IllegalStateException("There should be 1 resources in the list: fhir");
        }

        var list = new ArrayList<>(resources);
        var testDataBuilder = TestData.builder();

        var hl7v3Resource = list.stream()
            .filter(resource -> resource.getFilename() != null)
            .filter(resource -> resource.getFilename().endsWith(HL7V3_FILE_ENDING))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(String.format("Missing %s resource", HL7V3_FILE_ENDING)));

        testDataBuilder.hl7v3(TestData.readFile(hl7v3Resource));
        list.remove(hl7v3Resource);

        var fhirResource = list.get(0);
        var fhirFileName = fhirResource.getFilename();
        if (fhirFileName == null) {
            throw new IllegalStateException("Resource should be a file");
        }
        if (fhirFileName.endsWith(FHIR_JSON_FILE_ENDING)) {
            testDataBuilder.fhirFormat(FhirFormat.JSON);
        } else {
            throw new IllegalStateException(String.format(
                "Fhir resource should be %s", FHIR_JSON_FILE_ENDING));
        }
        testDataBuilder.fhir(TestData.readFile(fhirResource));

        return testDataBuilder.build();
    }

    @SneakyThrows
    private static String readFile(Resource resource) {
        return new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
    }

    @RequiredArgsConstructor
    public enum FhirFormat {
        JSON
    }
}
