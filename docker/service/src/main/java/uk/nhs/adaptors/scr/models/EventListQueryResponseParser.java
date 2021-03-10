package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import static javax.xml.xpath.XPathConstants.NODESET;
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

    private final XmlUtils xmlUtils;

    @SneakyThrows
    public EventListQueryResponse parseXml(Document soapEnvelope) {
        EventListQueryResponse response = new EventListQueryResponse();
        response.setViewPermission(getPermissionValue(soapEnvelope, VIEW_PERMISSION_XPATH));
        response.setStorePermission(getPermissionValue(soapEnvelope, STORE_PERMISSION_XPATH));
        response.setLatestScrId(xmlUtils.getNodeAttributeValue(soapEnvelope, LATEST_SCR_ID_XPATH, "root"));

        return response;
    }

    private AcsPermission getPermissionValue(Document soapEnvelope, String xPath) {
        boolean isErrorSet = isErrorSet(soapEnvelope);
        if (isErrorSet) {
            return NO;
        } else {
            String nodeText = xmlUtils.getNodeText(soapEnvelope, xPath);
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
