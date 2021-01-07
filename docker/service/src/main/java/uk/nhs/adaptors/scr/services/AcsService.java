package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.SpineClientContract;
import uk.nhs.adaptors.scr.clients.SpineHttpClient;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.AcsParams;
import uk.nhs.adaptors.scr.models.AcsPermission;

import java.time.format.DateTimeFormatter;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;
import static uk.nhs.adaptors.scr.config.ConversationIdFilter.CORRELATION_ID_MDC_KEY;
import static uk.nhs.adaptors.scr.utils.TemplateUtils.fillTemplate;
import static uk.nhs.adaptors.scr.utils.TemplateUtils.loadTemplate;

@Component
@Slf4j
public class AcsService {

    private static final String SET_PERMISSION_PARAM_NAME = "setPermissions";
    private static final String NHS_NUMBER_PART_NAME = "nhsNumber";
    private static final String PERMISSION_CODE_PART_NAME = "permissionCode";
    private static final String PERMISSION_CODE_SYSTEM = "https://fhir.nhs.uk/R4/CodeSystem/SCR-ACSPermission";

    @Autowired
    private SpineClientContract spineClient;

    @Autowired
    private ScrConfiguration scrConfiguration;

    @Autowired
    private SpineConfiguration spineConfiguration;

    private static final Mustache SET_RESOURCE_PERMISSIONS_TEMPLATE =
        loadTemplate("SET_RESOURCE_PERMISSIONS_INUK01.mustache");

    public SpineHttpClient.Response setPermission(Parameters parameters, String nhsdAsid, String clientIp) {
        ParametersParameterComponent parameter = getSetPermissionParameter(parameters);

        String acsRequest = prepareAcsRequest(getNhsNumber(parameter), getPermission(parameter), nhsdAsid, clientIp);
        return spineClient.sendAcsData(acsRequest, nhsdAsid);
    }

    private String prepareAcsRequest(String nhsNumber, AcsPermission permission, String nhsdAsid, String clientIp) {
        AcsParams acsParams = new AcsParams()
            .setGeneratedMessageId(MDC.get(CORRELATION_ID_MDC_KEY))
            .setMessageCreationTime(DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(now(UTC)))
            .setNhsNumber(nhsNumber)
            .setSenderFromASID(nhsdAsid)
            .setSpineToASID(scrConfiguration.getNhsdAsidTo())
            .setSpineAcsEndpointUrl(spineConfiguration.getUrl())
            .setPermissionValue(permission.getSpineValue())
            .setSenderHostIpAddress(clientIp);

        return fillTemplate(SET_RESOURCE_PERMISSIONS_TEMPLATE, acsParams);
    }

    private static String getNhsNumber(ParametersParameterComponent parameter) {
        return parameter.getPart().stream()
            .filter(p -> NHS_NUMBER_PART_NAME.equals(p.getName()))
            .reduce((x, y) -> {
                throw new FhirMappingException(String.format("Exactly 1 Parameter.Part named '%s' expected", NHS_NUMBER_PART_NAME));
            })
            .orElseThrow(() -> new FhirMappingException(String.format(
                "Parameter.Part named '%s' not found", NHS_NUMBER_PART_NAME))
            )
            .getValue()
            .toString();
    }

    private static AcsPermission getPermission(ParametersParameterComponent parameter) {
        Coding coding = (Coding) parameter.getPart().stream()
            .filter(p -> PERMISSION_CODE_PART_NAME.equals(p.getName()))
            .filter(p -> PERMISSION_CODE_SYSTEM.equals(((Coding) p.getValue()).getSystem()))
            .reduce((x, y) -> {
                throw new FhirMappingException(String.format("Exactly 1 Parameter.Part named '%s' with valueCoding.system %s expected",
                    PERMISSION_CODE_PART_NAME, PERMISSION_CODE_SYSTEM));
            })
            .orElseThrow(() -> new FhirMappingException(String.format(
                "Parameter.Part named '%s' with valueCoding.system %s not found", PERMISSION_CODE_PART_NAME, PERMISSION_CODE_SYSTEM))
            )
            .getValue();

        String permissionValue = coding.getCode();
        try {
            return AcsPermission.fromValue(permissionValue);
        } catch (Exception e) {
            LOGGER.error("Invalid permission value: " + permissionValue, e);
            throw new FhirMappingException(String.format("Invalid value - %s in field 'valueCoding.code'", permissionValue));
        }
    }

    private static ParametersParameterComponent getSetPermissionParameter(Parameters parameters) {
        return parameters.getParameter().stream()
            .filter(p -> SET_PERMISSION_PARAM_NAME.equals(p.getName()))
            .reduce((x, y) -> {
                throw new FhirMappingException(String.format("Exactly 1 parameter named '%s' expected", SET_PERMISSION_PARAM_NAME));
            })
            .orElseThrow(() -> new FhirMappingException(String.format(
                "Parameter named '%s' name not found", SET_PERMISSION_PARAM_NAME))
            );
    }
}
