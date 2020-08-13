package uk.nhs.adaptors.scr.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

public class ResourcesUtils {
    public static String getResourceAsString(String path) {
        try {
            return IOUtils.toString(new ClassPathResource(path).getInputStream(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
