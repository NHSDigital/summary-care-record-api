package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThirdPartyCorrespondenceNote {
    private String text;
    public ThirdPartyCorrespondenceNote(String text) {
        this.text = text;
    }
}
