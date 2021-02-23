package uk.nhs.adaptors.scr.mappings.from.hl7;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Narrative;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class HtmlParser {

    private static final String H2 = "h2";

    @SneakyThrows
    public List<Composition.SectionComponent> parse(Node html) {
        removeEmptyNodes(html);

        var bodyNode = XmlUtils.getNodesByXPath(html, "./body").stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Missing body node in html"));

        var childNodes = bodyNode.getChildNodes();
        if (childNodes.getLength() != 0 && !H2.equals(childNodes.item(0).getNodeName())) {
            throw new IllegalStateException("First body node must be H2");
        }

        // this will hold a list of paris: H2 node as key and all next nodes (until next H2 or body end) as a new Document
        var items = new ArrayList<Pair<Node, Document>>();
        Document targetDocument = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            var currentNode = childNodes.item(i);
            if (H2.equals(currentNode.getNodeName())) {
                //since this is the H2, we begin to capture following nodes as a new Document
                targetDocument = createNewDocument("div", "http://www.w3.org/1999/xhtml");
                items.add(Pair.of(currentNode, targetDocument));
            } else {
                if (targetDocument == null) {
                    throw new IllegalStateException("Target document not initialized");
                }
                targetDocument.getDocumentElement()
                    .appendChild(targetDocument.importNode(currentNode, true));
            }
        }

        return items.stream()
            .map(kv -> ParsedHtml
                .builder()
                .html(serialize(kv.getValue()))
                .h2Value(kv.getKey().getTextContent())
                .h2Id(Optional.ofNullable(kv.getKey().getAttributes())
                    .map(h2IdAttribute -> h2IdAttribute.getNamedItem("id"))
                    .map(Node::getNodeValue)
                    .orElse(null))
                .build())
            .map(HtmlParser::buildSectionComponent)
            .collect(Collectors.toList());
    }

    @SneakyThrows
    public static Document createNewDocument(String tag, String xmlns) {
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        var rootNode = document.createElement(tag);
        rootNode.setAttribute("xmlns", xmlns);
        document.appendChild(rootNode);
        return document;
    }

    @SneakyThrows
    public static String serialize(Document document) {
        var xmlOutput = new StreamResult(new StringWriter());
        var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), xmlOutput);
        return xmlOutput.getWriter().toString();
    }

    @SneakyThrows
    public static void removeEmptyNodes(Node document) {
        XPathExpression xpathExp = XPathFactory.newInstance().newXPath()
            .compile("//text()[normalize-space(.) = '']");
        NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            Node emptyTextNode = emptyTextNodes.item(i);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }
    }

    @SneakyThrows
    public static Document parseDocument(String xml) {
        return DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));
    }

    private static Composition.SectionComponent buildSectionComponent(ParsedHtml parsedHtml) {
        var sectionComponent = new Composition.SectionComponent()
            .setTitle(parsedHtml.h2Value)
            .setText(buildNarrative(parsedHtml.html));
        if (StringUtils.isNotBlank(parsedHtml.h2Id)) {
            sectionComponent.setCode(new CodeableConcept()
                .addCoding(new Coding()
                    .setCode(parsedHtml.h2Id)));
        }
        return sectionComponent;
    }

    private static Narrative buildNarrative(String html) {
        var narrative = new Narrative()
            .setStatus(Narrative.NarrativeStatus.GENERATED);
        narrative.setDivAsString(html);
        return narrative;
    }

    @Builder
    private static class ParsedHtml {
        private final String h2Id;
        private final String h2Value;
        private final String html;
    }
}
