package uk.nhs.adaptors.scr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@Slf4j
public class ScrApplication {
    public static void main(String[] args) {
        LOGGER.debug("Starting SCR APP");
        run(ScrApplication.class);
    }
}
