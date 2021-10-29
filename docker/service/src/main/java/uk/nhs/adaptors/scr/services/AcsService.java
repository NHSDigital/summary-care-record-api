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
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.clients.identity.IdentityServiceContract;
import uk.nhs.adaptors.scr.clients.identity.UserInfo;
import uk.nhs.adaptors.scr.clients.spine.SpineClientContract;
import uk.nhs.adaptors.scr.clients.spine.SpineHttpClient.Response;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;
import uk.nhs.adaptors.scr.models.AcsParams;
import uk.nhs.adaptors.scr.models.AcsPermission;
import uk.nhs.adaptors.scr.models.RequestData;

import java.time.format.DateTimeFormatter;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneOffset.UTC;
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
    private final SpineResponseParser spineResponseParser;
    private final SpineDetectedIssuesHandler spineDetectedIssuesHandler;

    private static final Mustache SET_RESOURCE_PERMISSIONS_TEMPLATE =
        loadTemplate("SET_RESOURCE_PERMISSIONS_INUK01.mustache");

    public void setPermission(RequestData requestData) {
        Parameters parameters = fhirParser.parseResource(requestData.getBody(), Parameters.class);
        ParametersParameterComponent parameter = getSetPermissionParameter(parameters);
        UserInfo userInfo = identityService.getUserInfo(requestData.getAuthorization());
        String acsRequest = prepareAcsRequest(parameter, requestData, getUserRoleCode(userInfo, requestData.getNhsdSessionUrid()),
            userInfo.getId());
        Response<Document> response = spineClient.sendAcsData(acsRequest, requestData.getNhsdAsid());
        spineDetectedIssuesHandler.handleDetectedIssues(spineResponseParser.getDetectedIssues(response.getBody()));
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
            .findFirst()
            .get()
            .getValue()
            .toString();
    }

    private static AcsPermission getPermission(ParametersParameterComponent parameter) {
        Coding coding = (Coding) parameter.getPart()
            .stream()
            .filter(p -> PERMISSION_CODE_PART_NAME.equals(p.getName()))
            .filter(p -> PERMISSION_CODE_SYSTEM.equals(((Coding) p.getValue()).getSystem()))
            .findFirst()
            .get()
            .getValue();

        String permissionValue = coding.getCode();
        return AcsPermission.fromValue(permissionValue);
    }

    private static ParametersParameterComponent getSetPermissionParameter(Parameters parameters) {
        return parameters.getParameter().stream()
            .filter(p -> SET_PERMISSION_PARAM_NAME.equals(p.getName()))
            .findFirst()
            .get();
    }
}
