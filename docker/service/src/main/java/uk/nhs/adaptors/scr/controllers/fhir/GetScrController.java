package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.controllers.validation.getscrid.GetScrIdRequest;
import uk.nhs.adaptors.scr.services.GetScrService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_IP;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_REQUEST_URL;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.scr.controllers.utils.UrlUtils.extractBaseUrl;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Validated
public class GetScrController {

    private static final String PATIENT_ID_PREFIX = "https://fhir.nhs.uk/Id/nhs-number|";

    private final FhirParser fhirParser;
    private final GetScrService getScrService;

    @GetMapping(path = "/DocumentReference",
        produces = {APPLICATION_FHIR_JSON_VALUE})
    @SuppressWarnings("checkstyle:parameternumber")
    @GetScrIdRequest
    public String getScrId(@RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
                           @RequestHeader(CLIENT_IP) @NotNull String clientIp,
                           @RequestHeader(CLIENT_REQUEST_URL) @NotNull String clientRequestUrl,
                           @RequestParam("patient") @NotNull String patient,
                           @RequestParam(required = false) String type,
                           @RequestParam(name = "_sort", required = false) String sort,
                           @RequestParam(name = "_count", required = false) Integer count,
                           HttpServletRequest request) {
        String nhsNumber = extractNhsNumber(patient);
        String baseUrl = extractBaseUrl(clientRequestUrl, request.getRequestURI());

        Bundle bundle = getScrService.getScrId(nhsNumber, nhsdAsid, clientIp, baseUrl);

        return fhirParser.encodeToJson(bundle);
    }

    private String extractNhsNumber(String patientId) {
        return patientId.replace(PATIENT_ID_PREFIX, "");
    }
}
