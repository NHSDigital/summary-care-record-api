package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.ScrTimeoutException;
import uk.nhs.adaptors.scr.models.RequestData;
import uk.nhs.adaptors.scr.services.UploadScrService;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Callable;

import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FhirController {
    private final FhirParser fhirParser;
    private final UploadScrService uploadScrService;
    private final SpineConfiguration spineConfiguration;
    private final ScrConfiguration scrConfiguration;

    @PostMapping(
        path = "/Bundle",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public WebAsyncTask<ResponseEntity<?>> acceptFhir(
        @RequestHeader("Content-Type") @NotNull MediaType contentType,
        @RequestHeader("Nhsd-Asid") @NotNull String nhsdAsid,
        @RequestBody String body) {
        LOGGER.debug("Using cfg: asid-from={} party-from={} asid-to={} party-to={}",
            nhsdAsid,
            scrConfiguration.getPartyIdFrom(),
            scrConfiguration.getNhsdAsidTo(),
            scrConfiguration.getPartyIdTo());

        var requestData = new RequestData();
        requestData.setBundle(fhirParser.parseResource(body, Bundle.class));
        requestData.setNhsdAsid(nhsdAsid);

        var mdcContextMap = MDC.getCopyOfContextMap();
        Callable<ResponseEntity<?>> callable = () -> {
            MDC.setContextMap(mdcContextMap);
            uploadScrService.handleFhir(requestData);
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(contentType)
                .build();
        };

        var task = new WebAsyncTask<>(spineConfiguration.getScrResultTimeout(), callable);
        task.onTimeout(() -> {
            throw new ScrTimeoutException();
        });

        return task;
    }
}
