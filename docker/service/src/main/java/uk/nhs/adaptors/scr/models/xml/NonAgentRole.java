package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonAgentRole {
    private final String tag;

    private String codeCode;
    private String codeDisplayName;
    private String name;

    public NonAgentRole(String tag) {
        this.tag = tag;
    }
}
