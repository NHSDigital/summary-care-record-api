package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.ScrTimeoutException;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;
import uk.nhs.adaptors.scr.models.RequestData;
import uk.nhs.adaptors.scr.services.UploadScrService;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Callable;

import static org.springframework.http.HttpStatus.CREATED;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_IP;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_IDENTITY;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_SESSION_URID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SendScrController {
    private final UploadScrService uploadScrService;
    private final SpineConfiguration spineConfiguration;
    private final ScrConfiguration scrConfiguration;

    @PostMapping(
        path = "/Bundle",
        consumes = {APPLICATION_FHIR_JSON_VALUE},
        produces = {APPLICATION_FHIR_JSON_VALUE})
    @LogExecutionTime
    public WebAsyncTask<ResponseEntity<?>> sendScr(
        @RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
        @RequestHeader(CLIENT_IP) @NotNull String clientIp,
        @RequestHeader(NHSD_IDENTITY) @NotNull String nhsdIdentity,
        @RequestHeader(NHSD_SESSION_URID) @NotNull String nhsdSessionUrid,
        @RequestBody String body) {
        LOGGER.info("Received Upload SCR request");
        LOGGER.debug("Using cfg: asid-from={} party-from={} asid-to={} party-to={} client-ip={} NHSD-Identity-UUID={} NHSD-Session-URID={}",
            nhsdAsid,
            scrConfiguration.getPartyIdFrom(),
            scrConfiguration.getNhsdAsidTo(),
            scrConfiguration.getPartyIdTo(),
            clientIp,
            nhsdIdentity,
            nhsdSessionUrid);

        LOGGER.info("TEST 0");

        var requestData = new RequestData();

        LOGGER.info("TEST 1 - set body.");
        requestData.setBody(body)
            .setNhsdAsid(nhsdAsid)
            .setClientIp(clientIp)
            .setNhsdIdentity(nhsdIdentity)
            .setNhsdSessionUrid(nhsdSessionUrid);

        LOGGER.info("TEST 2 - getCopyOfContextMap");
        var mdcContextMap = MDC.getCopyOfContextMap();
        Callable<ResponseEntity<?>> callable = () -> {
            MDC.setContextMap(mdcContextMap);
            uploadScrService.uploadScr(requestData);
            return ResponseEntity
                .status(CREATED)
                .build();
        };

        LOGGER.info("TEST 3 - WebAsyncTask");
        var task = new WebAsyncTask<>(spineConfiguration.getScrResultTimeout(), callable);
        LOGGER.info("TEST 4 - WebAsyncTask Complete");
        task.onTimeout(() -> {
            throw new ScrTimeoutException();
        });
        task.onError(() -> {
            LOGGER.info("Error encountered attempting to send data to Spine (SCR FHIR API). Spine could not process the data provided.");
            throw new Exception();
        });

        LOGGER.info("Upload SCR to Spine complete. Returning task.");
        return task;
    }
}
