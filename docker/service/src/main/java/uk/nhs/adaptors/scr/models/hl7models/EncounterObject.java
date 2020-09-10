package uk.nhs.adaptors.scr.models.hl7models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncounterObject {
    private String informantTime;
    private String informantTypeCode;
    private String performerModeDisplay;
    private String performerModeCode;
    private String authorTime;
    private String authorTypeCode;
    private String performerTypeCode;
    private String performerTime;
}
