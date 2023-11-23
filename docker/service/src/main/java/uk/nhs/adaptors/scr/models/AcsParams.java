package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Model of AcsParams, used by AcsService for the
 * Access Control Service, which in turn powers the $setPermission endpoint.
 */
@Getter
@Setter
public class AcsParams {
    private String senderHostIpAddress;
    private String spineAcsEndpointUrl;
    private String spineToASID;
    private String senderFromASID;
    private String generatedMessageId;
    private String messageCreationTime;
    private String nhsNumber;
    private String permissionValue;
    private String sdsRoleProfileId;
    private String sdsUserId;
    private String sdsJobRoleCode;
}
