package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

import static javax.xml.xpath.XPathConstants.NODESET;
import static uk.nhs.adaptors.scr.models.AcsPermission.ASK;

@Getter
public class EventListQueryResponse {

    private static final String VIEW_PERMISSION_XPATH =
        "//accessControlAssertion[descendant::code[text()='View'] and descendant::type[text()='SCR']]/permission";
    private static final String STORE_PERMISSION_XPATH =
        "//accessControlAssertion[descendant::code[text()='Store'] and descendant::type[text()='SCR']]/permission";
    private static final String LATEST_SCR_ID_XPATH = "(//*[local-name()='event']/*[local-name()='eventID'])[1]";

    private AcsPermission viewPermission;
    private AcsPermission storePermission;
    private String latestScrId;

    @SneakyThrows
    public static EventListQueryResponse parseXml(String xml) {
        var soapEnvelope = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));

        EventListQueryResponse response = new EventListQueryResponse();
        String viewNodeText = getNodeText(soapEnvelope, VIEW_PERMISSION_XPATH);
        response.viewPermission = viewNodeText != null ? AcsPermission.fromValue(viewNodeText) : ASK;
        String storeNodeText = getNodeText(soapEnvelope, STORE_PERMISSION_XPATH);
        response.storePermission = storeNodeText != null ? AcsPermission.fromValue(storeNodeText) : ASK;
        response.latestScrId = getNodeAttributeValue(soapEnvelope, LATEST_SCR_ID_XPATH, "root");


        return response;
    }

    @SneakyThrows
    private static String getNodeAttributeValue(Document document, String xpath, String attributeName) {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        NodeList nodeList = ((NodeList) xPathExpression.evaluate(document, NODESET));

        return nodeList.getLength() > 0
            ? nodeList.item(0).getAttributes().getNamedItem(attributeName).getNodeValue() : null;
    }

    @SneakyThrows
    private static String getNodeText(Document document, String xpath) {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        NodeList nodeList = ((NodeList) xPathExpression.evaluate(document, NODESET));

        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }
}
