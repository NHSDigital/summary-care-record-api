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
import java.util.ArrayList;
import java.util.List;

import static javax.xml.xpath.XPathConstants.NODESET;
import static org.springframework.util.CollectionUtils.isEmpty;

@Getter
public class AcsResponse {
    private static final String ERROR_REASON_CODE_XPATH = "//justifyingDetectedIssueEvent/code";
    private List<ErrorReason> errorReasons;

    @SneakyThrows
    public static AcsResponse parseXml(String xml) {
        var soapEnvelope = DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(new InputSource(new StringReader(xml)));

        AcsResponse response = new AcsResponse();
        response.errorReasons = getErrorReasons(soapEnvelope);

        return response;
    }

    public boolean isSuccessful() {
        return isEmpty(errorReasons);
    }

    @SneakyThrows
    private static List<ErrorReason> getErrorReasons(Document document) {
        XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile(ERROR_REASON_CODE_XPATH);
        NodeList nodeList = ((NodeList) xPathExpression.evaluate(document, NODESET));
        List<ErrorReason> errorReasons = new ArrayList<>();
        int noOfErrorReasons = nodeList.getLength();

        for (int i = 0; i < noOfErrorReasons; i++) {
            ErrorReason reason = new ErrorReason();
            reason.code = nodeList.item(i).getAttributes().getNamedItem("code").getNodeValue();
            reason.display = nodeList.item(i).getAttributes().getNamedItem("displayName").getNodeValue();
            errorReasons.add(reason);
        }

        return errorReasons;
    }

    @Getter
    public static class ErrorReason {
        private String code;
        private String display;
    }
}
