package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import static javax.xml.xpath.XPathConstants.NODESET;
import static uk.nhs.adaptors.scr.models.AcsPermission.ASK;
import static uk.nhs.adaptors.scr.models.AcsPermission.NO;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getNodeAttributeValue;
import static uk.nhs.adaptors.scr.utils.XmlUtils.getNodeText;

@Getter
public class EventListQueryResponse {

    private static final String VIEW_PERMISSION_XPATH =
        "//accessControlAssertion[descendant::code[text()='View'] and descendant::type[text()='SCR']]/permission";
    private static final String STORE_PERMISSION_XPATH =
        "//accessControlAssertion[descendant::code[text()='Store'] and descendant::type[text()='SCR']]/permission";
    private static final String LATEST_SCR_ID_XPATH = "(//*[local-name()='event']/*[local-name()='eventID'])[1]";
    private static final String ERROR_REASON_CODE_XPATH = "//justifyingDetectedIssueEvent/code";

    private AcsPermission viewPermission;
    private AcsPermission storePermission;
    private String latestScrId;

    @SneakyThrows
    public static EventListQueryResponse parseXml(Document soapEnvelope) {
        EventListQueryResponse response = new EventListQueryResponse();
        response.viewPermission = getPermissionValue(soapEnvelope, VIEW_PERMISSION_XPATH);
        response.storePermission = getPermissionValue(soapEnvelope, STORE_PERMISSION_XPATH);
        response.latestScrId = getNodeAttributeValue(soapEnvelope, LATEST_SCR_ID_XPATH, "root");

        return response;
    }

    private static AcsPermission getPermissionValue(Document soapEnvelope, String xPath) {
        boolean isErrorSet = isErrorSet(soapEnvelope);
        if (isErrorSet) {
            return NO;
        } else {
            String nodeText = getNodeText(soapEnvelope, xPath);
            return nodeText != null ? AcsPermission.fromValue(nodeText) : ASK;
        }
    }

    @SneakyThrows
    private static boolean isErrorSet(Document document) {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(ERROR_REASON_CODE_XPATH);
        NodeList nodeList = ((NodeList) xPathExpression.evaluate(document, NODESET));

        return nodeList.getLength() > 0;
    }
}
