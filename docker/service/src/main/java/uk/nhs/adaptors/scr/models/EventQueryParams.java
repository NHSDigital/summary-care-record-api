package uk.nhs.adaptors.scr.models;

import lombok.Data;

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
