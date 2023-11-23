package uk.nhs.adaptors.scr.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.utils.XmlUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * SpineResourceParser service.
 * Accepts an XML document and detects whether it contains a "Justifying Detected Issue Event" node. Returns list
 * of issue codes and names. Called after several Spine requests.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpineResponseParser {
    private static final String ERROR_REASON_CODE_XPATH = "//justifyingDetectedIssueEvent/code";
    private static final String ERROR_REASON_QUALIFIER_XPATH = "./qualifier";
    private static final String ERROR_QUALIFIER = "ER";

    private final XmlUtils xmlUtils;

    @SneakyThrows
    public List<DetectedIssueEvent> getDetectedIssues(Document document) {
        List<Node> issueEventNodes = xmlUtils.getNodesByXPath(document, ERROR_REASON_CODE_XPATH);

        return issueEventNodes.stream()
                .map(code -> {
                    DetectedIssueEvent issueEvent = new DetectedIssueEvent();
                    issueEvent.code = xmlUtils.getNodeAttributeValue(code, "code");
                    issueEvent.display = xmlUtils.getNodeAttributeValue(code, "displayName");
                    String qualifier = xmlUtils.getNodeAttributeValue(code, ERROR_REASON_QUALIFIER_XPATH, "code");
                    issueEvent.isError = ERROR_QUALIFIER.equals(qualifier);
                    return issueEvent;
                })
                .collect(toList());
    }

    @Getter
    public static class DetectedIssueEvent {
        private String code;
        private String display;
        private boolean isError;
    }
}
