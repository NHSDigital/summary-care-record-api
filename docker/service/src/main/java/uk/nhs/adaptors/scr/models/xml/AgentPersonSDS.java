package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentPersonSDS {
    private String idExtension;
    private PersonSDS agentPersonSDS;
}
