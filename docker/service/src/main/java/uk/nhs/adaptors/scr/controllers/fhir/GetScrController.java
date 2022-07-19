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
import uk.nhs.adaptors.scr.logging.LogExecutionTime;
import uk.nhs.adaptors.scr.services.GetScrService;
import uk.nhs.adaptors.scr.services.SdsService;
//import org.apache.commons.lang3.StringUtils;
//import uk.nhs.adaptors.scr.exceptions.BadRequestException;
//import java.net.URISyntaxException;

import javax.validation.constraints.NotNull;

import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_IP;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
//import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_SESSION_URID;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Validated
public class GetScrController {

    private static final String PATIENT_ID_PREFIX = "https://fhir.nhs.uk/Id/nhs-number|";
    private static final String COMPOSITION_PATIENT_ID_PREFIX =
        "composition.subject:Patient.identifier=https://fhir.nhs.uk/Id/nhs-number|";

    private final FhirParser fhirParser;
    private final GetScrService getScrService;
    private final SdsService sdsService;

    @GetMapping(path = "/DocumentReference",
        produces = {APPLICATION_FHIR_JSON_VALUE})
    @SuppressWarnings("checkstyle:parameternumber")
    @LogExecutionTime
    public String getScrId(@RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
                           @RequestHeader(CLIENT_IP) @NotNull String clientIp,
                           @RequestParam("patient") @NotNull @PatientId String patient,
                           @RequestParam(required = false) @TypeCode String type,
                           @RequestParam(name = "_sort", required = false) @SortMethod String sort,
                           @RequestParam(name = "_count", required = false) @RecordCount Integer count) {
        LOGGER.info("Received GET SCR ID request");
        String nhsNumber = extractNhsNumber(patient);
        Bundle bundle = getScrService.getScrId(nhsNumber, nhsdAsid, clientIp);

        return fhirParser.encodeToJson(bundle);
    }

    private String extractNhsNumber(String patientId) {
        return patientId.replace(PATIENT_ID_PREFIX, "");
    }

    @GetMapping(path = "/Bundle",
        produces = {APPLICATION_FHIR_JSON_VALUE})
    @LogExecutionTime
    public String getScr(
        @RequestHeader("Nhsd-Asid") @NotNull String nhsdAsid,
        @RequestHeader("client-ip") @NotNull String clientIp,
        @RequestParam("composition.identifier") @NotNull String compositionId,
        @RequestParam("composition.subject:Patient.identifier") @NotNull @PatientId String nhsNumber
    ) {
        LOGGER.info("Received GET SCR request");

        var bundle = getScrService.getScr(extractNhsNumber(nhsNumber), compositionId, nhsdAsid, clientIp);

        return fhirParser.encodeToJson(bundle);
    }

    /*
    * The following method is commented out as it should not be exposed in a production environment
    * However, it is incredibly useful for testing the set-up with SDS to retrieve the user role code.
    * To use (for test purposes), uncomment and uncomment relevant imports.
    * */
//    @GetMapping(path = "/RoleCheck",
//        produces = {APPLICATION_FHIR_JSON_VALUE})
//    @LogExecutionTime
//    public String getRole(
//        @RequestHeader(NHSD_SESSION_URID) @NotNull String nhsdSessionUrid
//    ) throws URISyntaxException {
//        LOGGER.info("Received GET SCR request");
//
//        var role = sdsService.getUserRoleCode(nhsdSessionUrid);
//
//        if (StringUtils.isAllEmpty(role)) {
//            throw new BadRequestException(String.format("Unable to determine SDS Job Role Code for "
//                + "the given RoleID: %s", nhsdSessionUrid));
//        }
//        return role;
//    }
}
