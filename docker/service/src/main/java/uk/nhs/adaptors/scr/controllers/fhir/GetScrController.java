package uk.nhs.adaptors.scr.controllers.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;
import uk.nhs.adaptors.scr.services.GetScrService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.CLIENT_IP;
import static uk.nhs.adaptors.scr.consts.ScrHttpHeaders.NHSD_ASID;
import static uk.nhs.adaptors.scr.controllers.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class GetScrController {

    private static final String PATIENT_ID_PREFIX = "https://fhir.nhs.uk/Id/nhs-number|";
    private static final String SUPPORTED_TYPE = "http://snomed.info/sct|196981000000101";
    private static final String SUPPORTED_SORT = "date";
    private static final Integer SUPPORTED_COUNT = 1;

    private final FhirParser fhirParser;
    private final GetScrService getScrService;


    @GetMapping(path = "/DocumentReference",
        produces = {APPLICATION_FHIR_JSON_VALUE})
    @SuppressWarnings("checkstyle:parameternumber")
    public String getScrId(@RequestHeader(NHSD_ASID) @NotNull String nhsdAsid,
                           @RequestHeader(CLIENT_IP) @NotNull String clientIp,
                           @RequestParam("patient") @NotNull String patient,
                           @RequestParam(required = false) String type,
                           @RequestParam(name = "_sort", required = false) String sort,
                           @RequestParam(name = "_count", required = false) Integer count,
                           HttpServletRequest request) {
        String nhsNumber = checkAndExtractNhsNumber(patient);
        checkGetScrTypeParam(type);
        checkGetScrSortParam(sort);
        checkGetScrCountParam(count);

        Bundle bundle = getScrService.getScrId(nhsNumber, nhsdAsid, clientIp);

        return fhirParser.encodeToJson(bundle);
    }

    private String checkAndExtractNhsNumber(String patientId) {
        if (isNotEmpty(patientId) && patientId.startsWith(PATIENT_ID_PREFIX)) {
            String nhsNumber = patientId.replace(PATIENT_ID_PREFIX, "");
            if (isNotEmpty(nhsNumber)) {
                return nhsNumber;
            }
        }

        throw new BadRequestException(String.format("Invalid value - %s in field 'patient'", patientId));
    }

    private void checkGetScrTypeParam(String type) {
        if (isNotEmpty(type) && !SUPPORTED_TYPE.equals(type)) {
            throw new BadRequestException(String.format("Invalid value - %s in field 'type'", type));
        }
    }

    private void checkGetScrSortParam(String sort) {
        if (isNotEmpty(sort) && !SUPPORTED_SORT.equals(sort)) {
            throw new BadRequestException(String.format("Invalid value - %s in field '_sort'", sort));
        }
    }

    private void checkGetScrCountParam(Integer count) {
        if (count != null && !SUPPORTED_COUNT.equals(count)) {
            throw new BadRequestException(String.format("Invalid value - %s in field '_count'", count));
        }
    }
}
