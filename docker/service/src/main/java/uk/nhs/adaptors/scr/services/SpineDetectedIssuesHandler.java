package uk.nhs.adaptors.scr.services;

import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;
import uk.nhs.adaptors.scr.exceptions.ForbiddenException;
import uk.nhs.adaptors.scr.exceptions.ServiceUnavailableException;
import uk.nhs.adaptors.scr.exceptions.UnexpectedSpineResponseException;
import uk.nhs.adaptors.scr.services.SpineResponseParser.DetectedIssueEvent;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * When certain HTTP response codes are detected, throw appropriate exceptions in our application.
 */
@Component
public class SpineDetectedIssuesHandler {

    private static final int GENERAL_REASON_INDEX = 0;
    private static final String INVALID_REQUEST = "400";
    private static final String DUPLICATE_MESSAGE_ID = "401";
    private static final String LOGICAL_INCONSISTENCY = "410";
    private static final String INVALID_UPDATE = "430";
    private static final String ACCESS_DENIED_BY_ACCESS_CONTROL = "420";
    private static final String SERVICE_NOT_AVAILABLE = "502";

    public void handleDetectedIssues(List<DetectedIssueEvent> detectedIssueEvents) {
        if (!isEmpty(detectedIssueEvents)) {
            DetectedIssueEvent generalIssue = detectedIssueEvents.get(GENERAL_REASON_INDEX);
            if (generalIssue.isError()) {
                String errorReason = detectedIssueEvents.stream()
                        .map(it -> String.format("Code: %s, Display: %s", it.getCode(), it.getDisplay()))
                        .collect(joining(";"));
                throwProperException(generalIssue.getCode(), errorReason);
            }
        }
    }

    private void throwProperException(String psisErrorCode, String message) {
        switch (psisErrorCode) {
            case INVALID_REQUEST:
            case DUPLICATE_MESSAGE_ID:
            case LOGICAL_INCONSISTENCY:
            case INVALID_UPDATE:
                throw new BadRequestException(message);
            case ACCESS_DENIED_BY_ACCESS_CONTROL:
                throw new ForbiddenException(message);
            case SERVICE_NOT_AVAILABLE:
                throw new ServiceUnavailableException(message);
            default:
                throw new UnexpectedSpineResponseException(message);
        }
    }
}
