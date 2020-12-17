package uk.nhs.adaptors.scr.uat.common;

import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static org.springframework.util.CollectionUtils.isEmpty;

@Builder
@Getter
public class TestData {
    public static final String REQUEST_FILE_ENDING = ".request.json";
    public static final String RESPONSE_FILE_ENDING = ".response.json";

    private final String fhirRequest;
    private final String fhirResponse;

    public static TestData build(List<Resource> resources) {
        if (isEmpty(resources)) {
            throw new IllegalStateException("There should be at least 1 resource in the list: request or response");
        }

        var list = new ArrayList<>(resources);
        var testDataBuilder = TestData.builder();

        list.stream()
            .filter(resource -> resource.getFilename() != null)
            .filter(resource -> resource.getFilename().endsWith(RESPONSE_FILE_ENDING))
            .findFirst()
            .ifPresent(it -> {
                testDataBuilder.fhirResponse(TestData.readFile(it));
                list.remove(it);
            });


        if (!isEmpty(list)) {
            var fhirResource = list.get(0);
            var fhirFileName = fhirResource.getFilename();
            if (fhirFileName == null) {
                throw new IllegalStateException("Resource should be a file");
            }
            testDataBuilder.fhirRequest(TestData.readFile(fhirResource));
        }
        return testDataBuilder.build();
    }

    @SneakyThrows
    private static String readFile(Resource resource) {
        return new String(readAllBytes(resource.getFile().toPath()), UTF_8);
    }
}
