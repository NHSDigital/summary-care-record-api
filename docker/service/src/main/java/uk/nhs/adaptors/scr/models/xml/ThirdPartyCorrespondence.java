package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThirdPartyCorrespondence {
    private String idRoot;
    private String codeCode;
    private String codeDisplayName;
    private String statusCodeCode;
    private String effectiveTimeLow;
    private ThirdPartyCorrespondenceNote note;
    public ThirdPartyCorrespondence() {
        this.note = new ThirdPartyCorrespondenceNote(
                "Additional information records have been found under the following types:");
    }
}
