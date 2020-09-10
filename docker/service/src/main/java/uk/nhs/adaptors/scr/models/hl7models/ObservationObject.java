package uk.nhs.adaptors.scr.models.hl7models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObservationObject {
    private String effectiveTimeLow;
    private String effectiveTimeHigh;
    private String centreTimeStamp;
    private String statusCode;
    private String findingCode;
    private String findingCodeSystem;
    private String findingDisplay;
    private String findingID;
}
