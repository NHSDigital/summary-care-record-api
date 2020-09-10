package uk.nhs.adaptors.scr.utils;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.StringReader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

import uk.nhs.adaptors.scr.models.hl7models.DocumentReferenceObject;

public class ScrResponseParser {
    private static final String EVENT_ID_NODE = "//*[@name='eventID']";

    public static String parseQueryListResponseXml(String xml) throws DocumentException {
        String uuid = "";

        if (isNotEmpty(xml)) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(xml));

            List<Node> names = document.selectNodes(EVENT_ID_NODE);
            if (names != null) {
                uuid = names.get(0).getStringValue();
            }
        }

        return uuid;
    }

    public static DocumentReferenceObject parseQueryResponseXml(String xml) throws DocumentException {
        DocumentReferenceObject documentReference = new DocumentReferenceObject();
        if (isNotEmpty(xml)) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(xml));

            Node eventID = document.selectSingleNode("//*[local-name()='eventID']");
            Node persistenceDate = document.selectSingleNode("//*[local-name()='persistenceDate']");
            List<Node> nhsNumber = document.selectNodes("//*[local-name()='patient']/*[local-name()='id']");
            List<Node> payloadID = document.selectNodes("//*[local-name()='payloadID']");
            List<Node> eventStatus = document.selectNodes("//*[local-name()='eventStatus']");
            List<Node> eventType = document.selectNodes("//*[local-name()='eventType']");

            //queryResponse.setHl7Data(document.selectSingleNode("//*[local-name()='hl7Data']").asXML());
            documentReference.setNhsNumber(((DefaultElement) nhsNumber.get(0)).attributes().get(1).getValue());
            documentReference.setEventID(((DefaultElement) eventID).attributes().get(0).getValue());
            documentReference.setDateDocumentCreated(((DefaultElement) persistenceDate).attributes().get(0).getValue());
            documentReference.setMessageCategory(((DefaultElement) payloadID.get(0)).attributes().get(1).getValue());
            documentReference.setEventStatus(((DefaultElement) eventStatus.get(0)).attributes().get(2).getValue());
            documentReference.setMessageType(((DefaultElement) eventType.get(0)).attributes().get(2).getValue());
        }
        return documentReference;
    }
}
