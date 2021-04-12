package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static uk.nhs.adaptors.scr.models.AcsPermission.ASK;
import static uk.nhs.adaptors.scr.models.AcsPermission.NO;

@Getter
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventListQueryResponseParser {

    private static final String VIEW_PERMISSION_XPATH =
        "//accessControlAssertion[descendant::code[text()='View'] and descendant::type[text()='SCR']]/permission";
    private static final String STORE_PERMISSION_XPATH =
        "//accessControlAssertion[descendant::code[text()='Store'] and descendant::type[text()='SCR']]/permission";
    private static final String LATEST_SCR_ID_XPATH = "(//*[local-name()='event']/*[local-name()='eventID'])[1]";
    private static final String ERROR_REASON_CODE_XPATH = "//justifyingDetectedIssueEvent/code";
    private static final String ERROR_REASON_CODE_CODE_XPATH = "./@code";
    private static final List<String> CASE_NOT_FOUND_ERROR_CODES = asList("210", "30312");
    private static final String SUCCESS_RESPONSE_WRAPPER_XPATH = "//QUPC_IN200000SM04";

    private final XmlUtils xmlUtils;

    @SneakyThrows
    public EventListQueryResponse parseXml(Document soapEnvelope) {
        EventListQueryResponse response = new EventListQueryResponse();
        if (isSuccessResponse(soapEnvelope) || isCaseNotFound(getErrors(soapEnvelope))) {
            response.setViewPermission(getPermissionValue(soapEnvelope, VIEW_PERMISSION_XPATH));
            response.setStorePermission(getPermissionValue(soapEnvelope, STORE_PERMISSION_XPATH));
            response.setLatestScrId(xmlUtils.getNodeAttributeValue(soapEnvelope, LATEST_SCR_ID_XPATH, "root"));
        } else {
            response.setViewPermission(NO);
            response.setStorePermission(NO);
        }

        return response;
    }

    private AcsPermission getPermissionValue(Document soapEnvelope, String xPath) {
        String nodeText = xmlUtils.getNodeText(soapEnvelope, xPath);
        return nodeText != null ? AcsPermission.fromValue(nodeText) : ASK;
    }

    @SneakyThrows
    private List<String> getErrors(Document document) {
        return xmlUtils.getNodesByXPath(document, ERROR_REASON_CODE_XPATH)
            .stream()
            .map(it -> xmlUtils.getValueByXPath(it, ERROR_REASON_CODE_CODE_XPATH))
            .sorted()
            .collect(toList());
    }

    private boolean isCaseNotFound(List<String> errors) {
        return CASE_NOT_FOUND_ERROR_CODES.equals(errors);
    }

    private boolean isSuccessResponse(Document document) {
        return xmlUtils.getOptionalNodeByXpath(document, SUCCESS_RESPONSE_WRAPPER_XPATH).isPresent();
    }
}
