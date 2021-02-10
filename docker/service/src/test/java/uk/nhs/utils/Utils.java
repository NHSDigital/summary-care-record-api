package uk.nhs.utils;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Utils {
    @SneakyThrows
    public static String readResourceFile(String filePath) {
        return new String(Files.readAllBytes(
            new ClassPathResource(filePath).getFile().toPath()),
            StandardCharsets.UTF_8);
    }

}
