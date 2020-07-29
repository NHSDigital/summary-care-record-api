package uk.nhs.adaptors.scr;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class ScrApplication {
    public static void main(String[] args) {
        run(ScrApplication.class);
    }
}
