package uk.nhs.adaptors.scr.uat.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public abstract class CustomArgumentsProvider implements ArgumentsProvider {

    private final String folder;

    public CustomArgumentsProvider(String folder) {
        this.folder = folder;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
        var resources = getResources();

        var grouped = Arrays.stream(resources)
            .filter(r -> r.getFilename() != null)
            .filter(r -> !r.getFilename().endsWith("txt")) // ignore notes
            .filter(r -> !r.getFilename().contains("ignore")) // ignore ignored
            .collect(Collectors.groupingBy(resource -> {
                var pathParts = ((FileSystemResource) resource).getPath().split("/");
                var category = pathParts[pathParts.length - 2];
                var fileName = pathParts[pathParts.length - 1];
                var name = fileName.split("\\.")[0];
                return category + "/" + name;
            })).entrySet().stream()
            .peek(es -> {
                if (es.getValue().size() != 2) {
                    throw new IllegalStateException(String.format(
                        "There should be 2 test data files: %s and one of [%s, %s]",
                        TestData.HL7V3_FILE_ENDING, TestData.FHIR_JSON_FILE_ENDING, TestData.FHIR_XML_FILE_ENDING));
                }
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                es -> TestData.build(es.getValue())));

        return grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(es -> Arguments.of(es.getKey(), es.getValue()));
    }

    private Resource[] getResources() throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        return resolver.getResources("classpath*:/" + folder + "/*/*");
    }

    public static class OutboundSuccess extends CustomArgumentsProvider {
        public OutboundSuccess() {
            super("outbound_uat_data");
        }
    }

    public static class OutboundInvalid extends CustomArgumentsProvider {
        public OutboundInvalid() {
            super("outbound_uat_invalid");
        }
    }
}
