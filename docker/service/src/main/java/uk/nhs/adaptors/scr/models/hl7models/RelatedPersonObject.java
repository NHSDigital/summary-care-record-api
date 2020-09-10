package uk.nhs.adaptors.scr.models.hl7models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelatedPersonObject {
    private String nonAgentPersonName;
    private String nonAgentRoleID;
    private String nonAgentRoleIDExtension;
    private String nonAgentRoleCode;
    private String nonAgentRoleCodeSystem;
    private String nonAgentRoleDisplayName;
}
