package uk.nhs.adaptors.scr.models.hl7models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PractitionerRoleObject {
    private String agentPersonSDSID; //done
    private String codeDisplayName;
    private String AgentPersonCode;
}
