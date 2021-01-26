package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.identity.IdentityServiceContract;
import uk.nhs.adaptors.scr.clients.identity.UserInfo;
import uk.nhs.adaptors.scr.clients.spine.SpineClientContract;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.AcsParams;
import uk.nhs.adaptors.scr.models.AcsPermission;
import uk.nhs.adaptors.scr.models.AcsResponse;
import uk.nhs.adaptors.scr.models.RequestData;

import java.time.format.DateTimeFormatter;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.joining;
import static uk.nhs.adaptors.scr.config.ConversationIdFilter.CORRELATION_ID_MDC_KEY;
import static uk.nhs.adaptors.scr.utils.TemplateUtils.fillTemplate;
import static uk.nhs.adaptors.scr.utils.TemplateUtils.loadTemplate;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcsService {

    private static final String SET_PERMISSION_PARAM_NAME = "setPermissions";
    private static final String NHS_NUMBER_PART_NAME = "nhsNumber";
    private static final String PERMISSION_CODE_PART_NAME = "permissionCode";
    private static final String PERMISSION_CODE_SYSTEM = "https://fhir.nhs.uk/CodeSystem/SCR-ACSPermission";

    private final SpineClientContract spineClient;
    private final ScrConfiguration scrConfiguration;
    private final SpineConfiguration spineConfiguration;
    private final FhirParser fhirParser;
    private final IdentityServiceContract identityService;

    private static final Mustache SET_RESOURCE_PERMISSIONS_TEMPLATE =
        loadTemplate("SET_RESOURCE_PERMISSIONS_INUK01.mustache");

    public void setPermission(RequestData requestData) {
        Parameters parameters = fhirParser.parseResource(requestData.getBody(), Parameters.class);
        ParametersParameterComponent parameter = getSetPermissionParameter(parameters);
        UserInfo userInfo = identityService.getUserInfo(requestData.getAuthorization());
        String acsRequest = prepareAcsRequest(parameter, requestData, getUserRoleCode(userInfo, requestData.getNhsdSessionUrid()),
            userInfo.getId());
        Response response = spineClient.sendAcsData(acsRequest, requestData.getNhsdAsid());
        AcsResponse acsResponse = AcsResponse.parseXml(response.getBody());
        if (!acsResponse.isSuccessful()) {
            handleUnsuccessfulOperation(acsResponse);
        }
    }

    private void handleUnsuccessfulOperation(AcsResponse acsResponse) {
        String errorReason = acsResponse.getErrorReasons().stream()
            .map(it -> String.format("Code: %s, Display: %s", it.getCode(), it.getDisplay()))
            .collect(joining(";"));

        throw new BadRequestException("Setting permissions failed due to following errors: " + errorReason);
    }

    private String getUserRoleCode(UserInfo userInfo, String nhsdSessionUrid) {
        return userInfo.getRoles().stream()
            .filter(role -> role.getPersonRoleId().equals(nhsdSessionUrid))
            .findFirst()
            .orElseThrow(() -> new BadRequestException(String.format("Unable to determine SDS Job Role Code for "
                + "the given RoleID: %s", nhsdSessionUrid)))
            .getRoleCode();
    }

    private String prepareAcsRequest(ParametersParameterComponent parameter, RequestData requestData, String sdsJobRoleCode,
                                     String nhsdIdentity) {
        AcsParams acsParams = new AcsParams()
            .setGeneratedMessageId(MDC.get(CORRELATION_ID_MDC_KEY))
            .setMessageCreationTime(DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(now(UTC)))
            .setNhsNumber(getNhsNumber(parameter))
            .setSenderFromASID(requestData.getNhsdAsid())
            .setSpineToASID(scrConfiguration.getNhsdAsidTo())
            .setSpineAcsEndpointUrl(spineConfiguration.getUrl())
            .setPermissionValue(getPermission(parameter).getSpineValue())
            .setSenderHostIpAddress(requestData.getClientIp())
            .setSdsRoleProfileId(requestData.getNhsdSessionUrid())
            .setSdsUserId(nhsdIdentity)
            .setSdsJobRoleCode(sdsJobRoleCode);

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
