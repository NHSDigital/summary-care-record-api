package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Device {
    private final String tag;

    private String idRoot;
    private String idExtension;
    private String codeCode;
    private String codeDisplayName;
    private String name;
    private String manufacturerModelName;
    private String description;

    public Device(String tag) {
        this.tag = tag;
    }
}
