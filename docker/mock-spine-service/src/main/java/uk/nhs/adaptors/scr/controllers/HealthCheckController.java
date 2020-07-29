package uk.nhs.adaptors.scr.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class HealthCheckController {

    @GetMapping("/healthcheck")
    public String get() {
        return "Spine mock service is working!";
    }
}
