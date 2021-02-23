package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonSDS {
    private final String tag;
    private String idExtension;
    private String name;

    public PersonSDS(String tag) {
        this.tag = tag;
    }
}
