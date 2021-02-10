package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationSDS {
    private final String tag;
    private String idRoot;
    private String idExtension;
    private String name;

    public OrganizationSDS(String tag) {
        this.tag = tag;
    }
}
