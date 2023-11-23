package uk.nhs.adaptors.scr.models;

import lombok.Data;
/**
 * EventList is integral to the requests to get the SCR from Spine, specifically, getting the latest SCR ID
 * It's therefore used heavily in GetSCRService.
 */
@Data
public class EventQueryParams {
    private String senderHostIpAddress;
    private String spinePsisEndpointUrl;
    private String spineToASID;
    private String senderFromASID;
    private String generatedMessageId;
    private String messageCreationTime;
    private String nhsNumber;
    private String psisEventId;
}
