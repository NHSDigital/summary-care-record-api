package uk.nhs.adaptors.scr.utils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import uk.nhs.adaptors.scr.models.responses.ConsentItem;

public class AcsResponseParser {
    private static final String ACCESS_CONTROL_ASSERTION_NODE = "//*[local-name()='accessControlAssertion']";
    private static final String PERMISSION_NODE = "//*[local-name()='permission']";
    private static final String USER_DATA_NODE = "//*[local-name()='userData']";
    private static final String RESOURCE_TYPE_NODE = "//*[local-name()='resource']/*[local-name()='type']";
    private static final String RESOURCE_ID_NODE = "//*[local-name()='resource']/*[local-name()='Id']";
    private static final String FUNCTION_CONTEXT_NODE = "//*[local-name()='function']/*[local-name()='context']";
    private static final String FUNCTION_CODE_NODE = "//*[local-name()='function']/*[local-name()='code']";
    private static final String ACCESSOR_TYPE_NODE = "//*[local-name()='accessor']/*[local-name()='type']";
    private static final String ACCESSOR_ID_NODE = "//*[local-name()='accessor']/*[local-name()='accessorId']/*[local-name()='id']";
    private static final String ACCESSOR_NAME_NODE = "//*[local-name()='accessor']/*[local-name()='accessorId']/*[local-name()='name']";

    public static List<ConsentItem> parseGetResourcePermissionsXml(String xml) throws DocumentException {
        List<ConsentItem> consents = new ArrayList<>();

        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(xml));

        List<Node> list = document.selectNodes(ACCESS_CONTROL_ASSERTION_NODE);
        if (list != null) {
            for (Node node : list) {
                ConsentItem consent = new ConsentItem();
                consent.setPermission(getOptionalValue(node, PERMISSION_NODE));
                consent.setUserData(getOptionalValue(node, USER_DATA_NODE));
                consent.setResourceType(getOptionalValue(node, RESOURCE_TYPE_NODE));
                consent.setResourceId(getOptionalValue(node, RESOURCE_ID_NODE));
                consent.setFunction(getOptionalValue(node, FUNCTION_CONTEXT_NODE));
                consent.setFunctionCode(getOptionalValue(node, FUNCTION_CODE_NODE));
                consent.setAccessorType(getOptionalValue(node, ACCESSOR_TYPE_NODE));
                consent.setAccessorId(getOptionalValue(node, ACCESSOR_ID_NODE));
                consent.setAccessorName(getOptionalValue(node, ACCESSOR_NAME_NODE));
                consents.add(consent);
            }
        }

        return consents;
    }

    private static String getOptionalValue(Node node, String xpath) {
        Node selectedNode = node.selectSingleNode(xpath);
        if (selectedNode == null) {
            return null;
        }
        return selectedNode.getText();
    }
}
