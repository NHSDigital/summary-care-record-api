package uk.nhs.adaptors.scr.uat.common;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CustomArgumentsProvider implements ArgumentsProvider {

    private static final String FHIR_FILE_ENDING = ".fhir.json";
    private static final String HL7V3_FILE_ENDING = ".hl7v3.xml";

    private final String folder;

    public CustomArgumentsProvider(String folder) {
        this.folder = folder;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
        var resources = getResources();

        var grouped = Arrays.stream(resources)
            .filter(r -> !r.getFilename().endsWith("txt")) // ignore notes
            .filter(r -> !r.getFilename().contains("ignore")) // ignore ignored
            .collect(Collectors.groupingBy(resource -> {
                var pathParts = ((FileSystemResource) resource).getPath().split("/");
                var category = pathParts[pathParts.length - 2];
                var fileName = pathParts[pathParts.length - 1];
                var fileNumber = fileName.split("\\.")[0];
                return category + "/" + fileNumber;
            })).entrySet().stream()
            .peek(es -> {
                if (es.getValue().size() != 2) {
                    throw new IllegalStateException(String.format(
                        "There should be 2 test data files: 'N.<any>%s' and 'N.<any>%s': %s", FHIR_FILE_ENDING, HL7V3_FILE_ENDING, es.getKey()));
                }
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                es -> TestData.builder()
                    .hl7v3(readResource(es.getValue(), HL7V3_FILE_ENDING))
                    .fhir(readResource(es.getValue(),FHIR_FILE_ENDING))
                    .build()));

        return grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(es -> Arguments.of(es.getKey(), es.getValue()));
    }

    private String readResource(List<Resource> resources, String fileEnding) {
        return resources.stream()
            .filter(resource -> resource.getFilename() != null)
            .filter(resource -> resource.getFilename().endsWith(fileEnding))
            .map(this::readFile)
            .findFirst()
            .orElseThrow();
    }

    @SneakyThrows
    private String readFile(Resource resource) {
        return new String(Files.readAllBytes(resource.getFile().toPath()));
    }

    private Resource[] getResources() throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        return resolver.getResources("classpath*:/" + folder + "/*/*");
    }

    public static class Outbound extends CustomArgumentsProvider {
        public Outbound() {
            super("outbound_uat_data");
        }
    }
}
