package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lifestyle {
    private String idRoot;
    private String codeCode;
    private String codeDisplayName;
    private String statusCodeCode;
    private String effectiveTimeLow;
    private String effectiveTimeHigh;
    private String effectiveTimeCenter;
    private Participant.Author author;
    private Participant.Informant informant;
}
