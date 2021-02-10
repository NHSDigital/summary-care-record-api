package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceSDS {
    private final String tag;

    private String idRoot;
    private String idExtension;

    public DeviceSDS(String tag) {
        this.tag = tag;
    }
}
