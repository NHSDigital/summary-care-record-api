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
import uk.nhs.adaptors.scr.controllers.validation.scr.PatientId;
import uk.nhs.adaptors.scr.controllers.validation.scr.RecordCount;
import uk.nhs.adaptors.scr.controllers.validation.scr.SortMethod;
import uk.nhs.adaptors.scr.controllers.validation.scr.TypeCode;
import uk.nhs.adaptors.scr.services.GetScrService;

import javax.validation.constraints.NotNull;

import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_IP;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

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
    public String getScrId(@RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
                           @RequestHeader(CLIENT_IP) @NotNull String clientIp,
                           @RequestParam("patient") @NotNull @PatientId String patient,
                           @RequestParam(required = false) @TypeCode String type,
                           @RequestParam(name = "_sort", required = false) @SortMethod String sort,
                           @RequestParam(name = "_count", required = false) @RecordCount Integer count) {
        String nhsNumber = extractNhsNumber(patient);
        Bundle bundle = getScrService.getScrId(nhsNumber, nhsdAsid, clientIp);

        return fhirParser.encodeToJson(bundle);
    }

    private String extractNhsNumber(String patientId) {
        return patientId.replace(PATIENT_ID_PREFIX, "");
    }
}
