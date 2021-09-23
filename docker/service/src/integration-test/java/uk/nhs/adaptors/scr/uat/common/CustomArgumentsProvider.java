package uk.nhs.adaptors.scr.uat.common;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.nhs.adaptors.scr.uat.common.TestData.REQUEST_FILE_ENDING;
import static uk.nhs.adaptors.scr.uat.common.TestData.RESPONSE_FILE_ENDING;

@RequiredArgsConstructor
public abstract class CustomArgumentsProvider implements ArgumentsProvider {

    private final String folder;
    private final String category;

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws IOException {
        var resources = getResources();

        return stream(resources)
            .filter(r -> r.getFilename() != null)
            .filter(r -> !r.getFilename().endsWith("txt")) // ignore notes
            .filter(r -> !r.getFilename().contains("ignore")) // ignore ignored
            .collect(Collectors.groupingBy(resource -> {
                var pathParts = ((FileSystemResource) resource).getPath().split("/");
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
            .sorted(comparing(Entry::getKey))
            .map(it -> Arguments.of(TestData.build(it.getValue())));
    }

    private Resource[] getResources() throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        return resolver.getResources("classpath*:/uat/" + folder + "/" + category + "/*");
    }

    public static class UploadScrSuccess extends CustomArgumentsProvider {
        public UploadScrSuccess() {
            super("upload_scr", "success");
        }
    }

    public static class UploadScrBadRequest extends CustomArgumentsProvider {
        public UploadScrBadRequest() {
            super("upload_scr", "bad_request");
        }
    }

    public static class UploadScrNoConsent extends CustomArgumentsProvider {
        public UploadScrNoConsent() {
            super("upload_scr", "no_consent");
        }
    }

    public static class UploadScrCaseNotFound extends CustomArgumentsProvider {
        public UploadScrCaseNotFound() {
            super("upload_scr", "not_found");
        }
    }

    public static class GetScrIdSuccess extends CustomArgumentsProvider {
        public GetScrIdSuccess() {
            super("get_scr_id", "success");
        }
    }

    public static class GetScrIdNoConsent extends CustomArgumentsProvider {
        public GetScrIdNoConsent() {
            super("get_scr_id", "no_consent");
        }
    }

    public static class GetScrSuccess extends CustomArgumentsProvider {
        public GetScrSuccess() {
            super("get_scr", "success");
        }
    }

    public static class GetScrInitialUploadSuccess extends CustomArgumentsProvider {
        public GetScrInitialUploadSuccess() {
            super("get_scr", "init_upload_success");
        }
    }

    public static class GetScrNoConsent extends CustomArgumentsProvider {
        public GetScrNoConsent() {
            super("get_scr", "no_consent");
        }
    }

    public static class SetAcsSuccess extends CustomArgumentsProvider {
        public SetAcsSuccess() {
            super("set_acs", "success");
        }
    }

    public static class SetAcsSpineError extends CustomArgumentsProvider {
        public SetAcsSpineError() {
            super("set_acs", "spine_error");
        }
    }

    public static class SetAcsBadRequest extends CustomArgumentsProvider {
        public SetAcsBadRequest() {
            super("set_acs", "bad_request");
        }
    }

    public static class SendAlertSuccess extends CustomArgumentsProvider {
        public SendAlertSuccess() {
            super("send_alert", "success");
        }
    }

    public static class SendAlertSpineError extends CustomArgumentsProvider {
        public SendAlertSpineError() {
            super("send_alert", "spine_error");
        }
    }

    public static class SendAlertBadRequest extends CustomArgumentsProvider {
        public SendAlertBadRequest() {
            super("send_alert", "bad_request");
        }
    }
}
