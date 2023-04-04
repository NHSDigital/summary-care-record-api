package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThirdPartyCorrespondenceNote {
    private String text;
    private String codeCode;
    private String codeDisplayName;
    public ThirdPartyCorrespondenceNote(String text) {
        this.text = text;
        this.codeCode = "SupportingText";
        this.codeDisplayName = "Supporting Text";
    }
}
