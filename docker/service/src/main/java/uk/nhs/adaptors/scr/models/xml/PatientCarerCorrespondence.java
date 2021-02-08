package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientCarerCorrespondence {
    private String idRoot;
    private String codeCode;
    private String codeDisplayName;
    private String statusCodeCode;
    private String effectiveTimeLow;
}
