package uk.nhs.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class PatientCarerCorrMapperArgumentsProvider implements ArgumentsProvider {

    @SneakyThrows
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Arrays.stream(getResources())
            .map(Resource::getFilename)
            .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
            .map(Arguments::of);
    }

    protected Resource[] getResources() throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        return resolver.getResources("classpath*:/patient_carer_correspondence/*.html");
    }
}
