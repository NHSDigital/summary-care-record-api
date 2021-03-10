package uk.nhs.adaptors.scr.utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.xml.xpath.XPathConstants.NODE;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.STRING;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class XmlUtils {

    private final XPathFactory xPathFactory;

    @SneakyThrows
    public String getNodeAttributeValue(Node node, String xpath, String attributeName) {
        XPathExpression xPathExpression = xPathFactory.newXPath().compile(xpath);
        NodeList nodeList = ((NodeList) xPathExpression.evaluate(node, NODESET));

        return nodeList.getLength() > 0
            ? nodeList.item(0).getAttributes().getNamedItem(attributeName).getNodeValue() : null;
    }

    @SneakyThrows
    public String getValueByXPath(Node node, String xpath) {
        return getOptionalValueByXPath(node, xpath)
            .orElseThrow(() -> new FhirMappingException(String.format("Unable to find %s in Spine response", xpath)));
    }

    @SneakyThrows
    public Optional<String> getOptionalValueByXPath(Node node, String xpath) {
        var xPathExpression = xPathFactory.newXPath().compile(xpath);
        return Optional.ofNullable(xPathExpression.evaluate(node, STRING))
            .map(String.class::cast)
            .filter(StringUtils::isNotBlank);
    }

    @SneakyThrows
    public List<Node> getNodesByXPath(Node node, String xpath) {
        NodeList nodeList = getNodeListByXPath(node, xpath);
        var nodes = new ArrayList<Node>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            nodes.add(nodeList.item(i));
        }
        return nodes;
    }

    @SneakyThrows
    public NodeList getNodeListByXPath(Node node, String xpath) {
        var xPathExpression = xPathFactory.newXPath().compile(xpath);
        return (NodeList) xPathExpression.evaluate(node, XPathConstants.NODESET);
    }

    public Node getNodeAndDetachFromParent(NodeList nodeList, int index) {
        Node node = nodeList.item(index);
        node.getParentNode().removeChild(node);

        return node;
    }

    @SneakyThrows
    public Node getNodeByXpath(Node root, String xpath) {
        var xPathExpression = xPathFactory.newXPath().compile(xpath);
        var node = (Node) xPathExpression.evaluate(root, XPathConstants.NODE);
        node.getParentNode().removeChild(node);

        return node;
    }

    @SneakyThrows
    public Optional<Node> getOptionalNodeByXpath(Node root, String xpath) {
        var xPathExpression = xPathFactory.newXPath().compile(xpath);
        Optional<Node> node = Optional.ofNullable(xPathExpression.evaluate(root, NODE))
            .map(Node.class::cast)
            .filter(it -> it != null);

        node.ifPresent(it -> it.getParentNode().removeChild(it));

        return node;
    }

    @SneakyThrows
    public String getNodeText(Node node, String xpath) {
        XPathExpression xPathExpression = xPathFactory.newXPath().compile(xpath);
        NodeList nodeList = ((NodeList) xPathExpression.evaluate(node, NODESET));

        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }
}
