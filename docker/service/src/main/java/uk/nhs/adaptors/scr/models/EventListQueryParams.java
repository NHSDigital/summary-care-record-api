package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventListQueryParams {
    private String senderHostIpAddress;
    private String spinePsisEndpointUrl;
    private String spineToASID;
    private String senderFromASID;
    private String generatedMessageId;
    private String messageCreationTime;
    private String nhsNumber;
}
