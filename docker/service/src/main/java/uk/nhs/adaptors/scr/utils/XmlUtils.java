package uk.nhs.adaptors.scr.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.STRING;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class XmlUtils {
    @SneakyThrows
    public static String getNodeAttributeValue(Node node, String xpath, String attributeName) {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        NodeList nodeList = ((NodeList) xPathExpression.evaluate(node, NODESET));

        return nodeList.getLength() > 0
            ? nodeList.item(0).getAttributes().getNamedItem(attributeName).getNodeValue() : null;
    }

    @SneakyThrows
    public static String getValueByXPath(Node node, String xpath) {
        return getOptionalValueByXPath(node, xpath)
            .orElseThrow(() -> new FhirMappingException(String.format("Unable to find %s in Spine response", xpath)));
    }

    @SneakyThrows
    public static Optional<String> getOptionalValueByXPath(Node node, String xpath) {
        var xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        return Optional.ofNullable(xPathExpression.evaluate(node, STRING))
            .map(String.class::cast)
            .filter(StringUtils::isNotBlank);
    }

    @SneakyThrows
    public static List<Node> getNodesByXPath(Node node, String xpath) {
        var xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        var nodeList = (NodeList) xPathExpression.evaluate(node, XPathConstants.NODESET);
        var nodes = new ArrayList<Node>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }

    @SneakyThrows
    public static Node getNodeByXpath(Node root, String xpath) {
        var xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        var node = (Node) xPathExpression.evaluate(root, XPathConstants.NODE);

        return node;
    }

    @SneakyThrows
    public static Optional<Node> getOptionalNodeByXpath(Node root, String xpath) {
        var xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        return Optional.ofNullable(xPathExpression.evaluate(root, NODE))
            .map(Node.class::cast)
            .filter(it -> it != null);
    }

    @SneakyThrows
    public static String getNodeText(Node node, String xpath) {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
        NodeList nodeList = ((NodeList) xPathExpression.evaluate(node, NODESET));

        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }

    @SneakyThrows
    public static DocumentBuilder documentBuilder() {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setAttribute(ACCESS_EXTERNAL_DTD, EMPTY);
        df.setAttribute(ACCESS_EXTERNAL_SCHEMA, EMPTY);
        return df.newDocumentBuilder();
    }
}
