package uk.nhs.adaptors.scr.uat.common;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.nhs.adaptors.scr.uat.common.TestData.REQUEST_FILE_ENDING;
import static uk.nhs.adaptors.scr.uat.common.TestData.RESPONSE_FILE_ENDING;

public abstract class CustomArgumentsProvider implements ArgumentsProvider {

    private final String folder;

    public CustomArgumentsProvider(String folder) {
        this.folder = folder;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
        var resources = getResources();

        var grouped = stream(resources)
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
                if (isEmpty(es.getValue())) {
                    throw new IllegalStateException(String.format(
                        "There should be at least 1 test data file: %s or %s", REQUEST_FILE_ENDING, RESPONSE_FILE_ENDING));
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

    public static class UploadScrSuccess extends CustomArgumentsProvider {
        public UploadScrSuccess() {
            super("upload_scr_uat_data");
        }
    }

    public static class UploadScrInvalid extends CustomArgumentsProvider {
        public UploadScrInvalid() {
            super("upload_scr_uat_invalid");
        }
    }

    public static class UploadScrForbidden extends CustomArgumentsProvider {
        public UploadScrForbidden() {
            super("upload_scr_uat_forbidden");
        }
    }

    public static class GetScrIdSuccess extends CustomArgumentsProvider {
        public GetScrIdSuccess() {
            super("get_scr_id_data");
        }
    }

    public static class GetScrIdNoConsent extends CustomArgumentsProvider {
        public GetScrIdNoConsent() {
            super("get_scr_id_no_consent");
        }
    }

    public static class SetAcsSuccess extends CustomArgumentsProvider {
        public SetAcsSuccess() {
            super("set_acs_success");
        }
    }

    public static class SetAcsInvalidNhsNumber extends CustomArgumentsProvider {
        public SetAcsInvalidNhsNumber() {
            super("set_acs_invalid_nhs_number");
        }
    }
}
