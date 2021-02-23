package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Organization {
    private final String tag;
    private String idRoot;
    private String idExtension;
    private String codeCode;
    private String name;
    private String address;
    private String telecom;

    public Organization(String tag) {
        this.tag = tag;
    }
}
