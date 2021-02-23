package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Person {
    private final String tag;
    private String name;

    public Person(String tag) {
        this.tag = tag;
    }
}
